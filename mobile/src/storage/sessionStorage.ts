import AsyncStorage from "@react-native-async-storage/async-storage";

import type { AuthResponse } from "../types";

const SESSION_KEY = "@sistema-shekinah/session";

export async function getSession() {
  const raw = await AsyncStorage.getItem(SESSION_KEY);
  return raw ? (JSON.parse(raw) as AuthResponse) : null;
}

export async function saveSession(session: AuthResponse) {
  await AsyncStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export async function clearSession() {
  await AsyncStorage.removeItem(SESSION_KEY);
}
