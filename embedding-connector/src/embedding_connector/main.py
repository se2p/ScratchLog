import logging
import os
import random
import sys
from contextlib import asynccontextmanager
from pathlib import Path

import uvicorn
from fastapi import FastAPI, Depends, Request
from typing import Final, Any
from collections.abc import AsyncIterator

from pydantic import BaseModel, Field

from embedding_connector.ggnn_api import ApiModel

log: Final[logging.Logger] = logging.getLogger("uvicorn")


@asynccontextmanager
async def _app_init(app: FastAPI) -> AsyncIterator[None]:
    log.info("Initialising model...")
    config_env = os.getenv("MODEL_CONFIG_FILE")
    if config_env is None:
        config = Path("/ggnn-model-config.yaml")
        log.warning(
            "Missing environment value 'GGNN_CONFIG_FILE'! Using default %s.", config
        )
    else:
        config = Path(config_env)
    # app.state.ggnn_model = ApiModel(config)
    app.state.ggnn_model = "dummy model"
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
def get_ggnn_embedding(
    req: GgnnEmbeddingRequest, model: ApiModel = Depends(_get_ggnn_model)
) -> GgnnEmbeddingResponse:
    log.info(model)
    log.info(req)
    # todo: query `model` and return actual embedding
    return GgnnEmbeddingResponse(embedding=list(range(128)))


ProcessedGgnnProgram = dict[str, Any]


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
def get_progress_variance_projection(
    req: GgnnProgressVarianceProjectionRequest,
    model: ApiModel = Depends(_get_ggnn_model),
) -> ProgressVarianceProjection:
    return ProgressVarianceProjection(
        projections=[
            Projection(id=project_id, xy=(random.random(), random.random()))
            for project_id, project in req.student_programs.items()
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
