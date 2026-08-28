const API_PORT = "8081";

function normalizeBaseUrl(url: string) {
  return url.trim().replace(/\/+$/, "");
}

function getDefaultApiBaseUrl() {
  const protocol = globalThis.location?.protocol || "http:";
  const hostname = globalThis.location?.hostname;

  if (!hostname) {
    return `http://localhost:${API_PORT}/api`;
  }

  return `${protocol}//${hostname}:${API_PORT}/api`;
}

export const API_BASE_URL = normalizeBaseUrl(
  import.meta.env.VITE_API_BASE_URL || getDefaultApiBaseUrl()
);
