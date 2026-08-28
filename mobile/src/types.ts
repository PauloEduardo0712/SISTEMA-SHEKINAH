export type Role = "ADMIN" | "VOLUNTARIO";
export type TimeSlot = "MANHA" | "NOITE";
export type AvailabilityStatus = "DISPONIVEL" | "INDISPONIVEL";

export type AuthResponse = {
  token: string;
  userId: number;
  volunteerId: number | null;
  username: string;
  role: Role;
};

export type CurrentUser = Omit<AuthResponse, "token">;

export type Ministry = {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
};

export type Volunteer = {
  id: number;
  fullName: string;
  username: string;
  email: string | null;
  phone: string | null;
  notes: string | null;
  active: boolean;
  ministries: Ministry[];
};

export type Schedule = {
  id: number;
  ministry: Ministry;
  volunteer: Volunteer;
  serviceDate: string;
  serviceTime: string;
  timeSlot: TimeSlot;
  roleName: string | null;
  location: string | null;
  eventName: string | null;
  notes: string | null;
  conflict: boolean;
  conflictMessage: string | null;
};

export type Availability = {
  id: number;
  dayOfWeek: string;
  timeSlot: TimeSlot;
  status: AvailabilityStatus;
};

export type Conflict = {
  scheduleId: number;
  volunteerName: string;
  ministryName: string;
  message: string;
};
