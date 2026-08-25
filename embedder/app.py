from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

MODEL_ID = "sentence-transformers/all-MiniLM-L6-v2"
DIMS = 384

app = FastAPI(title="LUC embedder")
model = SentenceTransformer(MODEL_ID)


class EmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=64)


@app.get("/health")
def health() -> dict[str, object]:
    return {"status": "ok", "model": MODEL_ID, "dims": DIMS}


@app.post("/embed")
def embed(request: EmbedRequest) -> dict[str, object]:
    vectors = model.encode(
        request.texts,
        normalize_embeddings=True,
        convert_to_numpy=True,
    )
    return {"embeddings": vectors.tolist(), "dims": DIMS, "model": MODEL_ID}
