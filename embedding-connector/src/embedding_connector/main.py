import sys
import uvicorn
from fastapi import FastAPI
from typing import Final

app: Final[FastAPI] = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}


def main(argv: list[str] | None = None) -> int:
    if argv is None:
        argv = sys.argv[1:]

    uvicorn.run(
        f"{__package__}.main:app", host="127.0.0.1", port=8080, log_level="info"
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
