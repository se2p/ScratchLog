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

from embedding_connector.ggnn_api import ApiModel
from embedding_connector.projection import progress_variance_projection

log: Final[logging.Logger] = logging.getLogger("uvicorn")


@asynccontextmanager
async def _app_init(app: FastAPI) -> AsyncIterator[None]:
    log.info("Initialising model...")
    config_env = os.getenv("MODEL_CONFIG_FILE")
    if config_env is None:
        config = Path("/ggnn/model-config.yaml")
        log.warning(
            "Missing environment value 'GGNN_CONFIG_FILE'! Using default %s.", config
        )
    else:
        config = Path(config_env)
    app.state.ggnn_model = ApiModel(config)
    log.info("Model has been initialised successfully!")
    yield


app: Final[FastAPI] = FastAPI(lifespan=_app_init)


def _get_ggnn_model(request: Request) -> ApiModel:
    return request.app.state.ggnn_model


@app.get("/")
def read_root():
    return {"Hello": "World"}


class GgnnEmbeddingRequest(BaseModel):
    processed_project: str


class GgnnEmbeddingResponse(BaseModel):
    embedding: list[float]


@app.post("/ggnn/embedding/")
async def get_ggnn_embedding(
    req: GgnnEmbeddingRequest, model: ApiModel = Depends(_get_ggnn_model)
) -> GgnnEmbeddingResponse:
    embedding = await model.embed(req.processed_project)
    return GgnnEmbeddingResponse(embedding=embedding.tolist())


ProcessedGgnnProgram = str


class GgnnProgressVarianceProjectionRequest(BaseModel):
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
async def get_progress_variance_projection(
    req: GgnnProgressVarianceProjectionRequest,
    model: ApiModel = Depends(_get_ggnn_model),
) -> ProgressVarianceProjection:
    if len(req.student_programs) == 0:
        return ProgressVarianceProjection(projections=[])

    start_embedding = await model.embed(req.template_program)
    solution_embedding = await model.embed(req.solution_program)
    embeddings = [
        (project_id, await model.embed(project))
        for project_id, project in req.student_programs.items()
    ]
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


def main(argv: list[str] | None = None) -> int:
    if argv is None:
        argv = sys.argv[1:]

    uvicorn.run(
        f"{__package__}.main:app", host="127.0.0.1", port=8080, log_level="info"
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
