# Code Embedding Connector

Uses [uv](https://docs.astral.sh/uv/) for dependency management.


## Starting

```bash
# install dependencies
uv sync
# run the application on port 8080 (add `--port=OTHER` to change the port)
uv run embedding-connector
```


## Environment variables

- GGNN variables: only required when using the GGNN model
  - `GGNN_MODEL_CONFIG`: path to a GGNN model configuration YAML
- LLM variables: only required when using an LLM API endpoint
  - `LLM_API_ENDPOINT`: base URL for the OpenAI-compatible LLM provider API, e.g. `http://localhost:11434/v1/` for a local Ollama instance
  - `LLM_API_KEY`: the access key for the LLM provider API
  - `LLM_MODEL`: the model that should be used
