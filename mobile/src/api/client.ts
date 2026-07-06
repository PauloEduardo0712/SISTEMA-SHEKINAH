import Constants from "expo-constants";
import { Platform } from "react-native";

import type { AuthResponse } from "../types";

const fallbackHost = Platform.OS === "android" ? "10.0.2.2" : "localhost";
const expoHost = getExpoHost();
const fallbackUrl = `http://${expoHost ?? fallbackHost}:8081/api`;

export const API_BASE_URL = normalizeBaseUrl(process.env.EXPO_PUBLIC_API_BASE_URL || fallbackUrl);

type RequestOptions = {
  token?: string;
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
};

export async function login(username: string, password: string) {
  return request<AuthResponse>("/auth/login", {
    method: "POST",
    body: { username, password },
  });
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

function normalizeBaseUrl(url: string) {
  return url.trim().replace(/\/+$/, "");
}

function getExpoHost() {
  const hostUri = Constants.expoConfig?.hostUri;
  if (!hostUri) {
    return null;
  }

  const host = hostUri.split(":")[0];
  return host && host !== "127.0.0.1" ? host : null;
}

async function readError(response: Response) {
  try {
    const data = await response.json();
    return data.message || data.error || "Erro na comunicacao com o servidor.";
  } catch {
    return "Erro na comunicacao com o servidor.";
  }
}
