import type { AuthResponse, Availability, Ministry, Schedule, Volunteer } from "../types";

let active = false;
const ministries: Ministry[] = [
  { id: 1, name: "Louvor", description: "Ministerio de musica e adoracao", active: true },
  { id: 2, name: "Recepcao", description: "Acolhimento da igreja", active: true },
  { id: 3, name: "Midia", description: "Projecao e transmissao", active: true },
];
const volunteers: Volunteer[] = [
  { id: 1, fullName: "Ana Souza", username: "ana", email: "ana@shekinah.org", phone: "(11) 99999-0001", notes: null, active: true, ministries: [ministries[0]] },
  { id: 2, fullName: "Carlos Lima", username: "carlos", email: "carlos@shekinah.org", phone: "(11) 99999-0002", notes: null, active: true, ministries: [ministries[1]] },
  { id: 3, fullName: "Maria Oliveira", username: "maria", email: "maria@shekinah.org", phone: "(11) 99999-0003", notes: null, active: true, ministries: [ministries[2]] },
];
const date = (days: number) => {
  const value = new Date();
  value.setDate(value.getDate() + days);
  return value.toISOString().slice(0, 10);
};
const schedules: Schedule[] = [
  { id: 1, ministry: ministries[0], volunteer: volunteers[0], serviceDate: date(2), serviceTime: "19:00:00", timeSlot: "NOITE", roleName: "Vocal", location: "Templo principal", eventName: "Culto de celebracao", notes: "Chegar 30 minutos antes", conflict: false, conflictMessage: null },
  { id: 2, ministry: ministries[1], volunteer: volunteers[1], serviceDate: date(4), serviceTime: "18:30:00", timeSlot: "NOITE", roleName: "Boas-vindas", location: "Entrada", eventName: "Culto de domingo", notes: null, conflict: false, conflictMessage: null },
  { id: 3, ministry: ministries[2], volunteer: volunteers[2], serviceDate: date(7), serviceTime: "19:00:00", timeSlot: "NOITE", roleName: "Projecao", location: "Cabine de midia", eventName: "Culto de oracao", notes: null, conflict: false, conflictMessage: null },
];
let availability: Availability[] = [
  { id: 1, dayOfWeek: "SUNDAY", timeSlot: "NOITE", status: "DISPONIVEL" },
  { id: 2, dayOfWeek: "WEDNESDAY", timeSlot: "NOITE", status: "DISPONIVEL" },
  { id: 3, dayOfWeek: "SATURDAY", timeSlot: "MANHA", status: "INDISPONIVEL" },
];

export const isDemoMode = () => active;
export const enableDemoMode = () => { active = true; };
export const disableDemoMode = () => { active = false; };
export function mockLogin(): AuthResponse {
  active = true;
  return { token: "demo-session", userId: 1, volunteerId: null, username: "admin (demo)", role: "ADMIN" };
}

export async function demoRequest<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const method = options.method ?? "GET";
  const endpoint = path.split("?")[0];
  let result: unknown = [];
  if (endpoint === "/auth/me") result = { userId: 1, volunteerId: null, username: "admin (demo)", role: "ADMIN" };
  else if (endpoint === "/ministries") result = method === "POST" ? ministries[0] : ministries;
  else if (endpoint === "/volunteers") result = method === "POST" ? volunteers[0] : volunteers;
  else if (endpoint === "/volunteers/me") result = volunteers[0];
  else if (endpoint === "/schedules/conflicts") result = [];
  else if (endpoint === "/schedules" || endpoint === "/schedules/me") result = method === "POST" ? schedules[0] : schedules;
  else if (endpoint === "/availabilities/me") {
    if (method === "PUT") availability = (options.body as Availability[]).map((item, index) => ({ ...item, id: index + 1 }));
    result = availability;
  }
  return result as T;
}
