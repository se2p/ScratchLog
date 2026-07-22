import argparse
import logging
import os
import sys
from contextlib import asynccontextmanager
from pathlib import Path

import numpy
import uvicorn
from fastapi import FastAPI, Depends, Request
from typing import Final
from collections.abc import AsyncIterator

from pydantic import BaseModel, Field

from embedding_connector.ggnn_api import GgnnApiModel
from embedding_connector.llm_api import LlmApiModel
from embedding_connector.projection import progress_variance_projection

log: Final[logging.Logger] = logging.getLogger("uvicorn")


@asynccontextmanager
async def _app_init(app: FastAPI) -> AsyncIterator[None]:
    log.info("Initialising model...")
    config_env = os.getenv("GGNN_CONFIG_FILE")
    if config_env is not None:
        config = Path(config_env)
        log.info("Loading GGNN config from %s", config)
        app.state.ggnn_model = GgnnApiModel(config)

    llm_endpoint = os.getenv("LLM_API_ENDPOINT")
    llm_key = os.getenv("LLM_API_KEY")
    llm_model = os.getenv("LLM_MODEL")
    if llm_endpoint is not None:
        if llm_model is None:
            e = "Missing LLM_MODEL model name environment variable!"
            raise ValueError(e)

        app.state.llm_model = LlmApiModel(
            llm_endpoint,
            llm_key if llm_key is not None else "empty",
            llm_model,
        )

    log.info("Model has been initialised successfully!")
    yield


app: Final[FastAPI] = FastAPI(lifespan=_app_init)


def _get_ggnn_model(request: Request) -> GgnnApiModel:
    return request.app.state.ggnn_model


def _get_llm_model(request: Request) -> LlmApiModel:
    return request.app.state.llm_model


class GgnnEmbeddingRequest(BaseModel):
    processed_project: str


class GgnnEmbeddingResponse(BaseModel):
    embedding: list[float]


@app.post("/ggnn/embedding/")
async def get_ggnn_embedding(
    req: GgnnEmbeddingRequest, model: GgnnApiModel = Depends(_get_ggnn_model)
) -> GgnnEmbeddingResponse:
    embedding = await model.embed(req.processed_project)
    return GgnnEmbeddingResponse(embedding=embedding.tolist())


ProcessedGgnnProgram = str
Embedding = numpy.typing.NDArray[numpy.float64]


class ProgressVarianceProjectionRequest(BaseModel):
    template_program: ProcessedGgnnProgram = Field(..., alias="templateProgram")
    solution_program: ProcessedGgnnProgram = Field(..., alias="solutionProgram")
    student_programs: dict[int, ProcessedGgnnProgram] = Field(
        ..., alias="studentPrograms"
    )


class Projection(BaseModel):
    id: int
    xy: tuple[float, float]


class ProgressVarianceProjection(BaseModel):
    projections: list[Projection]


@app.post("/ggnn/progress-variance-projection")
async def get_progress_variance_projection_ggnn(
    req: ProgressVarianceProjectionRequest,
    model: GgnnApiModel = Depends(_get_ggnn_model),
) -> ProgressVarianceProjection:
    if len(req.student_programs) == 0:
        return ProgressVarianceProjection(projections=[])

    start_embedding = await model.embed(req.template_program)
    solution_embedding = await model.embed(req.solution_program)
    embeddings = [
        (project_id, await model.embed(project))
        for project_id, project in req.student_programs.items()
    ]
    return _pv_projection(start_embedding, solution_embedding, embeddings)


@app.post("/llm/progress-variance-projection")
async def get_progress_variance_projection_llm(
    req: ProgressVarianceProjectionRequest,
    model: LlmApiModel = Depends(_get_llm_model),
) -> ProgressVarianceProjection:
    if len(req.student_programs) == 0:
        return ProgressVarianceProjection(projections=[])

    start_embedding = model.embed_single(req.template_program)
    solution_embedding = model.embed_single(req.solution_program)
    embeddings = [
        (project_id, model.embed_single(project))
        for project_id, project in req.student_programs.items()
    ]
    return _pv_projection(start_embedding, solution_embedding, embeddings)


def _pv_projection(
    start_embedding: Embedding,
    solution_embedding: Embedding,
    embeddings: list[tuple[int, Embedding]],
) -> ProgressVarianceProjection:
    student_embeddings = numpy.stack([e[1] for e in embeddings])
    projections = progress_variance_projection(
        student_embeddings, start_embedding, solution_embedding
    )

    return ProgressVarianceProjection(
        projections=[
            Projection(id=project_id, xy=(projection[0], projection[1]))
            for project_id, projection in zip(
                (e[0] for e in embeddings),
                projections,
                strict=True,
            )
        ]
    )


class EmbeddingDistanceRequest(BaseModel):
    solution_program: ProcessedGgnnProgram = Field(..., alias="solutionProgram")
    student_programs: dict[int, ProcessedGgnnProgram] = Field(
        ..., alias="studentPrograms"
    )


class EmbeddingDistance(BaseModel):
    id: int
    d: float


class EmbeddingDistanceResponse(BaseModel):
    distances: list[EmbeddingDistance]


@app.post("/ggnn/embedding-distance")
async def get_embedding_distance_ggnn(
    req: EmbeddingDistanceRequest, model: GgnnApiModel = Depends(_get_ggnn_model)
) -> EmbeddingDistanceResponse:
    if len(req.student_programs) == 0:
        return EmbeddingDistanceResponse(distances=[])

    solution_embedding = await model.embed(req.solution_program)
    embeddings = {
        project_id: await model.embed(project)
        for project_id, project in req.student_programs.items()
    }
    return _distances(solution_embedding, embeddings)


@app.post("/llm/embedding-distance")
async def get_embedding_distance_llm(
    req: EmbeddingDistanceRequest, model: LlmApiModel = Depends(_get_llm_model)
) -> EmbeddingDistanceResponse:
    if len(req.student_programs) == 0:
        return EmbeddingDistanceResponse(distances=[])

    solution_embedding = model.embed_single(req.solution_program)
    embeddings = {
        project_id: model.embed_single(project)
        for project_id, project in req.student_programs.items()
    }
    return _distances(solution_embedding, embeddings)


def _distances(
    solution_embedding: Embedding, embeddings: dict[int, Embedding]
) -> EmbeddingDistanceResponse:
    distances = {
        project_id: numpy.linalg.norm(embedding - solution_embedding)
        for project_id, embedding in embeddings.items()
    }
    max_distance = max(distances.values())
    distances = [
        EmbeddingDistance(id=project_id, d=d / max_distance)
        for project_id, d in distances.items()
    ]

    return EmbeddingDistanceResponse(distances=distances)


def main(argv: list[str] | None = None) -> int:
    if argv is None:
        argv = sys.argv[1:]

    args = _build_arg_parser().parse_args(argv)

    uvicorn.run(
        f"{__package__}.main:app", host="127.0.0.1", port=args.port, log_level="info"
    )

    return 0


def _build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8080, required=False)
    return parser


if __name__ == "__main__":
    raise SystemExit(main())
