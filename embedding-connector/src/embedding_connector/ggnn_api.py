import asyncio
import functools
import logging
from pathlib import Path
from typing import Final

import numpy
from ggnn.config import load_config
from ggnn.program_embedding.train import ProgramEmbeddingTool

log: Final[logging.Logger] = logging.getLogger(__name__)


class GgnnApiModel:
    _lock: asyncio.Lock
    _embedding_tool: ProgramEmbeddingTool

    def __init__(self, config_file: Path) -> None:
        self._lock = asyncio.Lock()

        config = load_config(config_file, None, eval_only=True)
        self._embedding_tool = ProgramEmbeddingTool(config, embedding_per_sprite=True)

    async def embed(self, program: str) -> numpy.typing.NDArray[numpy.float64]:
        async with self._lock:
            per_sprite_embeddings = self._compute(program)

        return numpy.mean(per_sprite_embeddings, axis=0)

    @functools.lru_cache(maxsize=5_000)
    def _compute(self, program: str) -> list[float]:
        return self._embedding_tool.embedding(program)
