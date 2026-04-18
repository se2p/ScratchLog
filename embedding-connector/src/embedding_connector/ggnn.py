import asyncio
import json
import logging
from pathlib import Path
from typing import Final

import torch
from ggnn import shared
from ggnn.config import load_config
from ggnn.evaluate import predictions_to_labels
from ggnn.model import Ggnn
from ggnn.node_label_dataset import GraphVocabulary
from ggnn.sprite_processing import SpriteProcessor, Sprite
from pydantic import BaseModel


log: Final[logging.Logger] = logging.getLogger(__name__)


class Prediction(BaseModel):
    predicted_sub_tokens: list[list[str]]
    predicted_scores: list[float]


# todo: replace with embedding generator already present in GGNN code
class ApiModel:
    model: Ggnn
    vocab: GraphVocabulary
    preprocessor: SpriteProcessor
    _lock: asyncio.Lock

    def __init__(self, config_file: Path) -> None:
        self._lock = asyncio.Lock()

        config = load_config(config_file, None, eval_only=True)

        if config.finetuning_config:
            self.vocab = shared.prepare_vocabulary_for_finetuning(config)
        else:
            self.vocab = shared.prepare_vocabulary(config)

        self.preprocessor = SpriteProcessor(
            vocab=self.vocab,
            max_label_length=config.model_config.max_label_length,
            max_type_length=config.model_config.max_type_length,
        )

        if config.finetuning_config:
            checkpoint_dir = config.finetuning_config.checkpoints_dir
        else:
            checkpoint_dir = config.checkpoints_dir

        # do not load last, but best one
        checkpoint = shared.load_checkpoint(checkpoint_dir, last=False)
        if checkpoint is None:
            msg = f"No checkpoint found in {config.checkpoints_dir}."
            raise ValueError(msg)

        self.model = shared.build_beam_model(
            config.model_config, self.vocab, checkpoint.model_state
        )
        self.model.eval()

        pytorch_total_params = sum(
            p.numel() for p in self.model.parameters() if p.requires_grad
        )
        log.info("Model has %d parameters", pytorch_total_params)

    async def predict(self, source_codes: list[str], top_k: int) -> list[Prediction]:
        self.model.decoder.set_beam_width(top_k)  # type: ignore

        result = []

        for item in source_codes:
            data = json.loads(item)
            sprite = Sprite(
                label=data["label"],
                label_nodes=data["labelNodes"],
                graph=data["contextGraph"],
            )
            datapoint = self.preprocessor.process_sprite(sprite)
            batch = datapoint.to(shared.get_device())

            node_batch_map = torch.zeros(
                size=(batch.x.shape[0],), device=shared.get_device()
            )
            target_nodes = torch.tensor([batch.root_node], device=shared.get_device())
            node_counts = torch.tensor([batch.node_count], device=shared.get_device())

            async with self._lock:
                with torch.no_grad():
                    output = self.model(
                        x=batch.x,
                        edge_index=batch.edge_index,
                        node_batch_map=node_batch_map,
                        target_nodes=target_nodes,
                        node_counts=node_counts,
                        y=None,
                    )

            predictions = predictions_to_labels(self.vocab, [t[1] for t in output])
            result.append(
                Prediction(
                    predicted_sub_tokens=predictions,
                    predicted_scores=[t[0] for t in output],
                )
            )

        return result
