import datetime
import functools
import logging
from timeit import default_timer as timer
from typing import Final

import numpy
import openai

_log: Final[logging.Logger] = logging.getLogger(__name__)


class LlmApiModel:
    _client: openai.OpenAI
    _api_key: str
    _model: str

    def __init__(self, api_endpoint: str, api_key: str, model: str) -> None:
        self._client = openai.OpenAI(base_url=api_endpoint, api_key=api_key)
        self._model = model

    def embed(self, programs: list[str]) -> numpy.typing.NDArray[numpy.float64]:
        start = timer()
        batch = self._client.embeddings.create(
            model=self._model,
            input=programs,
        )
        end = timer()
        _log.info(
            "Computed an %s embedding in %s.",
            self._model,
            datetime.timedelta(seconds=end - start),
        )
        return numpy.stack([e.embedding for e in batch.data])

    @functools.lru_cache(maxsize=5_000)
    def embed_single(self, program: str) -> numpy.typing.NDArray[numpy.float64]:
        return self.embed([program])[0]
