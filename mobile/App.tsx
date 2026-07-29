import { StatusBar } from "expo-status-bar";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Modal,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { API_BASE_URL, login, request } from "./src/api/client";
import { AppButton } from "./src/components/AppButton";
import { AppCard } from "./src/components/AppCard";
import { EmptyState } from "./src/components/EmptyState";
import { TextField } from "./src/components/TextField";
import { clearSession, getSession, saveSession } from "./src/storage/sessionStorage";
import { colors, radius, spacing, typography } from "./src/theme";
import type {
  AuthResponse,
  Availability,
  AvailabilityStatus,
  Conflict,
  CurrentUser,
  Ministry,
  Schedule,
  TimeSlot,
  Volunteer,
} from "./src/types";

type TabKey = "agenda" | "disponibilidade" | "voluntarios" | "ministerios" | "admin";
type LoginMode = "entrar" | "cadastro";

const tabs: Array<{ key: TabKey; label: string; adminOnly?: boolean }> = [
  { key: "agenda", label: "Agenda" },
  { key: "disponibilidade", label: "Disponibilidade" },
  { key: "voluntarios", label: "Voluntarios", adminOnly: true },
  { key: "ministerios", label: "Ministerios", adminOnly: true },
  { key: "admin", label: "Painel", adminOnly: true },
];

const dayLabels: Record<string, string> = {
  MONDAY: "Segunda",
  TUESDAY: "Terca",
  WEDNESDAY: "Quarta",
  THURSDAY: "Quinta",
  FRIDAY: "Sexta",
  SATURDAY: "Sabado",
  SUNDAY: "Domingo",
};

const timeSlotLabels: Record<TimeSlot, string> = {
  MANHA: "Manha",
  NOITE: "Noite",
};

const statusLabels: Record<AvailabilityStatus, string> = {
  DISPONIVEL: "Disponivel",
  INDISPONIVEL: "Indisponivel",
};

const emptyVolunteerForm = {
  fullName: "",
  username: "",
  password: "1234",
  email: "",
  phone: "",
  notes: "",
  ministryId: "",
};

const emptyMinistryForm = {
  name: "",
  description: "",
};

const emptyScheduleForm = {
  ministryId: "",
  volunteerId: "",
  serviceDate: new Date().toISOString().slice(0, 10),
  serviceTime: "19:00",
  timeSlot: "NOITE" as TimeSlot,
  roleName: "",
  location: "",
  eventName: "",
  notes: "",
};

export default function App() {
  const [session, setSession] = useState<AuthResponse | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [booting, setBooting] = useState(true);
  const [tab, setTab] = useState<TabKey>("agenda");

  const signOut = useCallback(async () => {
    await clearSession();
    setSession(null);
    setCurrentUser(null);
    setTab("agenda");
  }, []);

  const loadCurrentUser = useCallback(async (token: string) => {
    const user = await request<CurrentUser>("/auth/me", { token });
    setCurrentUser(user);
  }, []);

  useEffect(() => {
    async function restore() {
      try {
        const stored = await getSession();
        if (stored?.token) {
          setSession(stored);
          await loadCurrentUser(stored.token);
        }
      } catch {
        await clearSession();
      } finally {
        setBooting(false);
      }
    }

    restore();
  }, [loadCurrentUser]);

  const handleAuth = useCallback(
    async (auth: AuthResponse) => {
      await saveSession(auth);
      setSession(auth);
      await loadCurrentUser(auth.token);
    },
    [loadCurrentUser],
  );

  const isAdmin = (currentUser?.role ?? session?.role) === "ADMIN";
  const visibleTabs = useMemo(() => tabs.filter(item => !item.adminOnly || isAdmin), [isAdmin]);

  if (booting) {
    return (
      <SafeAreaView style={styles.centerScreen}>
        <StatusBar style="dark" />
        <ActivityIndicator color={colors.primary} />
        <Text style={styles.loadingText}>Carregando sessao...</Text>
      </SafeAreaView>
    );
  }

  if (!session) {
    return (
      <>
        <StatusBar style="light" />
        <LoginScreen onAuthenticated={handleAuth} />
      </>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <StatusBar style="dark" />
      <View style={styles.header}>
        <View style={styles.headerText}>
          <Text style={styles.kicker}>Sistema de Escalas</Text>
          <Text style={styles.title}>Shekinah IAD</Text>
          <Text style={styles.subtitle}>{currentUser?.username ?? session.username}</Text>
        </View>
        <AppButton label="Sair" variant="ghost" onPress={signOut} />
      </View>

      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.tabs}>
        {visibleTabs.map(item => (
          <Pressable
            key={item.key}
            onPress={() => setTab(item.key)}
            style={[styles.tabButton, tab === item.key && styles.tabButtonActive]}
          >
            <Text style={[styles.tabText, tab === item.key && styles.tabTextActive]}>{item.label}</Text>
          </Pressable>
        ))}
      </ScrollView>

      {tab === "agenda" && <SchedulesScreen token={session.token} isAdmin={isAdmin} />}
      {tab === "disponibilidade" && <AvailabilityScreen token={session.token} />}
      {tab === "voluntarios" && isAdmin && <VolunteersScreen token={session.token} />}
      {tab === "ministerios" && isAdmin && <MinistriesScreen token={session.token} />}
      {tab === "admin" && isAdmin && <AdminScreen token={session.token} />}
    </SafeAreaView>
  );
}

function LoginScreen({ onAuthenticated }: { onAuthenticated: (auth: AuthResponse) => Promise<void> }) {
  const [mode, setMode] = useState<LoginMode>("entrar");
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("1234");
  const [ministries, setMinistries] = useState<Ministry[]>([]);
  const [registerForm, setRegisterForm] = useState({ ...emptyVolunteerForm, password: "" });
  const [loading, setLoading] = useState(false);
  const [ministriesLoading, setMinistriesLoading] = useState(true);
  const [ministriesError, setMinistriesError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const activeMinistries = ministries.filter(item => item.active);

  const loadMinistries = useCallback(async () => {
    setMinistriesLoading(true);
    setMinistriesError(null);

    try {
      const data = await request<Ministry[]>("/ministries");
      setMinistries(data);
      if (data.filter(item => item.active).length === 0) {
        setMinistriesError("Nenhum ministerio ativo foi encontrado. Entre como admin e cadastre ou ative um ministerio.");
      }
    } catch (err) {
      setMinistries([]);
      setMinistriesError(
        err instanceof Error
          ? `${err.message} API: ${API_BASE_URL}`
          : `Nao foi possivel carregar ministerios. API: ${API_BASE_URL}`,
      );
    } finally {
      setMinistriesLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMinistries();
  }, [loadMinistries]);

  async function submitLogin() {
    setError(null);
    setLoading(true);

    try {
      const auth = await login(username.trim(), password);
      await onAuthenticated(auth);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel entrar.");
    } finally {
      setLoading(false);
    }
  }

  async function submitRegister() {
    setError(null);
    setLoading(true);

    try {
      const auth = await request<AuthResponse>("/auth/register", {
        method: "POST",
        body: {
          fullName: registerForm.fullName.trim(),
          username: registerForm.username.trim(),
          password: registerForm.password,
          email: registerForm.email.trim(),
          phone: registerForm.phone.trim(),
          notes: registerForm.notes.trim(),
          ministryIds: [Number(registerForm.ministryId)],
        },
      });
      await onAuthenticated(auth);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel criar a conta.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.loginScreen}>
      <ScrollView contentContainerStyle={styles.loginContent} keyboardShouldPersistTaps="handled">
        <View style={styles.loginHero}>
          <Text style={styles.loginKicker}>Shekinah IAD</Text>
          <Text style={styles.loginTitle}>Escalas na palma da mao</Text>
          <Text style={styles.loginText}>Entre, consulte a agenda e atualize sua disponibilidade pelo app nativo.</Text>
        </View>

        <AppCard style={styles.loginCard}>
          <View style={styles.switchRow}>
            <Segment label="Entrar" active={mode === "entrar"} onPress={() => setMode("entrar")} />
            <Segment label="Criar conta" active={mode === "cadastro"} onPress={() => setMode("cadastro")} />
          </View>

          {mode === "entrar" ? (
            <>
              <TextField label="Usuario" value={username} onChangeText={setUsername} autoCapitalize="none" />
              <TextField label="Senha" value={password} onChangeText={setPassword} secureTextEntry />
              <AppButton label="Entrar" loading={loading} onPress={submitLogin} />
            </>
          ) : (
            <>
              <TextField label="Nome completo" value={registerForm.fullName} onChangeText={fullName => setRegisterForm({ ...registerForm, fullName })} />
              <TextField label="Usuario" value={registerForm.username} onChangeText={value => setRegisterForm({ ...registerForm, username: value })} autoCapitalize="none" />
              <TextField label="Senha" value={registerForm.password} onChangeText={value => setRegisterForm({ ...registerForm, password: value })} secureTextEntry />
              <OptionGrid
                label="Ministerio"
                value={registerForm.ministryId}
                options={activeMinistries.map(item => ({ label: item.name, value: String(item.id) }))}
                onChange={ministryId => setRegisterForm({ ...registerForm, ministryId })}
              />
              {ministriesLoading ? (
                <View style={styles.inlineLoading}>
                  <ActivityIndicator color={colors.primary} size="small" />
                  <Text style={styles.mutedText}>Carregando ministerios...</Text>
                </View>
              ) : null}
              {ministriesError ? (
                <View style={styles.warningBox}>
                  <Text style={styles.warningText}>{ministriesError}</Text>
                  <AppButton label="Tentar novamente" variant="secondary" loading={ministriesLoading} onPress={loadMinistries} />
                </View>
              ) : null}
              <TextField label="E-mail" value={registerForm.email} onChangeText={email => setRegisterForm({ ...registerForm, email })} keyboardType="email-address" autoCapitalize="none" />
              <TextField label="Telefone" value={registerForm.phone} onChangeText={phone => setRegisterForm({ ...registerForm, phone })} keyboardType="phone-pad" />
              <AppButton label="Criar conta" loading={loading} disabled={!registerForm.ministryId || ministriesLoading || activeMinistries.length === 0} onPress={submitRegister} />
            </>
          )}

          {error ? <Text style={styles.errorText}>{error}</Text> : null}
        </AppCard>
      </ScrollView>
    </SafeAreaView>
  );
}

function SchedulesScreen({ token, isAdmin }: { token: string; isAdmin: boolean }) {
  const [items, setItems] = useState<Schedule[]>([]);
  const [ministries, setMinistries] = useState<Ministry[]>([]);
  const [volunteers, setVolunteers] = useState<Volunteer[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyScheduleForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    const endpoint = isAdmin ? "/schedules" : "/schedules/me";
    const requests: Array<Promise<unknown>> = [request<Schedule[]>(endpoint, { token })];
    if (isAdmin) {
      requests.push(request<Ministry[]>("/ministries", { token }), request<Volunteer[]>("/volunteers", { token }));
    }
    const [scheduleData, ministryData, volunteerData] = await Promise.all(requests);
    setItems(scheduleData as Schedule[]);
    if (isAdmin) {
      setMinistries(ministryData as Ministry[]);
      setVolunteers(volunteerData as Volunteer[]);
    }
  }, [isAdmin, token]);

  useEffect(() => {
    load()
      .catch(err => setError(err instanceof Error ? err.message : "Erro ao carregar escalas."))
      .finally(() => setLoading(false));
  }, [load]);

  async function refresh() {
    setRefreshing(true);
    try {
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao atualizar escalas.");
    } finally {
      setRefreshing(false);
    }
  }

  async function createSchedule() {
    setSaving(true);
    try {
      await request<Schedule>("/schedules", {
        token,
        method: "POST",
        body: {
          ...form,
          ministryId: Number(form.ministryId),
          volunteerId: Number(form.volunteerId),
          serviceTime: normalizeTime(form.serviceTime),
        },
      });
      setForm(emptyScheduleForm);
      setFormOpen(false);
      await load();
    } catch (err) {
      Alert.alert("Nao foi possivel salvar", err instanceof Error ? err.message : "Tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingBlock label="Buscando escalas..." />;
  }

  return (
    <>
      <FlatList
        contentContainerStyle={styles.listContent}
        data={items}
        keyExtractor={item => String(item.id)}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} />}
        ListHeaderComponent={
          <>
            {isAdmin ? <AppButton label="Nova escala" onPress={() => setFormOpen(true)} /> : null}
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </>
        }
        ListEmptyComponent={<EmptyState title="Nenhuma escala encontrada" text="Quando houver escalas, elas aparecem aqui." />}
        renderItem={({ item }) => <ScheduleCard schedule={item} />}
      />

      <Sheet visible={formOpen} title="Nova escala" onClose={() => setFormOpen(false)}>
        <OptionGrid label="Ministerio" value={form.ministryId} options={ministries.map(item => ({ label: item.name, value: String(item.id) }))} onChange={ministryId => setForm({ ...form, ministryId })} />
        <OptionGrid label="Voluntario" value={form.volunteerId} options={volunteers.map(item => ({ label: item.fullName, value: String(item.id) }))} onChange={volunteerId => setForm({ ...form, volunteerId })} />
        <TextField label="Data" value={form.serviceDate} onChangeText={serviceDate => setForm({ ...form, serviceDate })} placeholder="AAAA-MM-DD" />
        <TextField label="Horario" value={form.serviceTime} onChangeText={serviceTime => setForm({ ...form, serviceTime })} placeholder="19:00" />
        <OptionGrid label="Turno" value={form.timeSlot} options={[{ label: "Manha", value: "MANHA" }, { label: "Noite", value: "NOITE" }]} onChange={timeSlot => setForm({ ...form, timeSlot: timeSlot as TimeSlot })} />
        <TextField label="Funcao" value={form.roleName} onChangeText={roleName => setForm({ ...form, roleName })} />
        <TextField label="Local" value={form.location} onChangeText={location => setForm({ ...form, location })} />
        <TextField label="Evento" value={form.eventName} onChangeText={eventName => setForm({ ...form, eventName })} />
        <TextField label="Observacoes" value={form.notes} onChangeText={notes => setForm({ ...form, notes })} multiline />
        <AppButton label="Salvar escala" loading={saving} disabled={!form.ministryId || !form.volunteerId} onPress={createSchedule} />
      </Sheet>
    </>
  );
}

function ScheduleCard({ schedule }: { schedule: Schedule }) {
  return (
    <AppCard style={styles.scheduleCard}>
      <View style={styles.rowBetween}>
        <Text style={styles.cardTitle}>{schedule.eventName || schedule.ministry.name}</Text>
        {schedule.conflict ? <Text style={styles.conflictBadge}>Conflito</Text> : null}
      </View>
      <Text style={styles.cardMeta}>
        {formatDate(schedule.serviceDate)} as {formatTime(schedule.serviceTime)} - {timeSlotLabels[schedule.timeSlot]}
      </Text>
      <Text style={styles.cardText}>{schedule.ministry.name}</Text>
      <Text style={styles.cardText}>{schedule.volunteer.fullName}</Text>
      {schedule.roleName ? <Text style={styles.cardText}>Funcao: {schedule.roleName}</Text> : null}
      {schedule.location ? <Text style={styles.cardText}>Local: {schedule.location}</Text> : null}
      {schedule.notes ? <Text style={styles.noteText}>{schedule.notes}</Text> : null}
      {schedule.conflictMessage ? <Text style={styles.errorText}>{schedule.conflictMessage}</Text> : null}
    </AppCard>
  );
}

function AvailabilityScreen({ token }: { token: string }) {
  const [items, setItems] = useState<Availability[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    const data = await request<Availability[]>("/availabilities/me", { token });
    setItems(data);
  }, [token]);

  useEffect(() => {
    load()
      .catch(err => setError(err instanceof Error ? err.message : "Erro ao carregar disponibilidade."))
      .finally(() => setLoading(false));
  }, [load]);

  async function toggle(item: Availability) {
    const nextStatus: AvailabilityStatus = item.status === "DISPONIVEL" ? "INDISPONIVEL" : "DISPONIVEL";
    const previous = items;
    const nextItems = items.map(current => (current.id === item.id ? { ...current, status: nextStatus } : current));
    setItems(nextItems);
    setSavingId(item.id);

    try {
      const payload = nextItems.map(({ dayOfWeek, timeSlot, status }) => ({ dayOfWeek, timeSlot, status }));
      const saved = await request<Availability[]>("/availabilities/me", {
        token,
        method: "PUT",
        body: payload,
      });
      setItems(saved);
    } catch (err) {
      setItems(previous);
      Alert.alert("Nao foi possivel salvar", err instanceof Error ? err.message : "Tente novamente.");
    } finally {
      setSavingId(null);
    }
  }

  if (loading) {
    return <LoadingBlock label="Buscando disponibilidade..." />;
  }

  return (
    <ScrollView contentContainerStyle={styles.listContent}>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
      {items.length === 0 ? (
        <EmptyState title="Disponibilidade vazia" text="Fale com um administrador para iniciar sua grade." />
      ) : (
        items.map(item => (
          <AppCard key={item.id} style={styles.availabilityCard}>
            <View style={styles.flexOne}>
              <Text style={styles.cardTitle}>{dayLabels[item.dayOfWeek] ?? item.dayOfWeek}</Text>
              <Text style={styles.cardMeta}>{timeSlotLabels[item.timeSlot]}</Text>
            </View>
            <AppButton
              label={savingId === item.id ? "Salvando" : statusLabels[item.status]}
              variant={item.status === "DISPONIVEL" ? "primary" : "secondary"}
              loading={savingId === item.id}
              onPress={() => toggle(item)}
            />
          </AppCard>
        ))
      )}
    </ScrollView>
  );
}

function VolunteersScreen({ token }: { token: string }) {
  const [items, setItems] = useState<Volunteer[]>([]);
  const [ministries, setMinistries] = useState<Ministry[]>([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyVolunteerForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    const [volunteerData, ministryData] = await Promise.all([
      request<Volunteer[]>("/volunteers", { token }),
      request<Ministry[]>("/ministries", { token }),
    ]);
    setItems(volunteerData);
    setMinistries(ministryData);
  }, [token]);

  useEffect(() => {
    load()
      .catch(err => setError(err instanceof Error ? err.message : "Erro ao carregar voluntarios."))
      .finally(() => setLoading(false));
  }, [load]);

  async function saveVolunteer() {
    setSaving(true);
    try {
      await request<Volunteer>("/volunteers", {
        token,
        method: "POST",
        body: {
          fullName: form.fullName.trim(),
          username: form.username.trim(),
          password: form.password,
          email: form.email.trim(),
          phone: form.phone.trim(),
          notes: form.notes.trim(),
          ministryIds: [Number(form.ministryId)],
          active: true,
        },
      });
      setForm(emptyVolunteerForm);
      setFormOpen(false);
      await load();
    } catch (err) {
      Alert.alert("Nao foi possivel salvar", err instanceof Error ? err.message : "Tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingBlock label="Carregando voluntarios..." />;
  }

  return (
    <>
      <FlatList
        contentContainerStyle={styles.listContent}
        data={items}
        keyExtractor={item => String(item.id)}
        ListHeaderComponent={
          <>
            <AppButton label="Novo voluntario" onPress={() => setFormOpen(true)} />
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </>
        }
        ListEmptyComponent={<EmptyState title="Nenhum voluntario" text="Cadastre voluntarios para montar as escalas." />}
        renderItem={({ item }) => (
          <AppCard style={styles.scheduleCard}>
            <Text style={styles.cardTitle}>{item.fullName}</Text>
            <Text style={styles.cardMeta}>{item.username}</Text>
            <Text style={styles.cardText}>{item.ministries.map(ministry => ministry.name).join(", ") || "Sem ministerio"}</Text>
            {item.phone ? <Text style={styles.noteText}>{item.phone}</Text> : null}
          </AppCard>
        )}
      />

      <Sheet visible={formOpen} title="Novo voluntario" onClose={() => setFormOpen(false)}>
        <TextField label="Nome completo" value={form.fullName} onChangeText={fullName => setForm({ ...form, fullName })} />
        <TextField label="Usuario" value={form.username} onChangeText={username => setForm({ ...form, username })} autoCapitalize="none" />
        <TextField label="Senha" value={form.password} onChangeText={password => setForm({ ...form, password })} secureTextEntry />
        <OptionGrid label="Ministerio" value={form.ministryId} options={ministries.map(item => ({ label: item.name, value: String(item.id) }))} onChange={ministryId => setForm({ ...form, ministryId })} />
        <TextField label="E-mail" value={form.email} onChangeText={email => setForm({ ...form, email })} autoCapitalize="none" keyboardType="email-address" />
        <TextField label="Telefone" value={form.phone} onChangeText={phone => setForm({ ...form, phone })} keyboardType="phone-pad" />
        <TextField label="Observacoes" value={form.notes} onChangeText={notes => setForm({ ...form, notes })} multiline />
        <AppButton label="Salvar voluntario" loading={saving} disabled={!form.fullName || !form.username || !form.password || !form.ministryId} onPress={saveVolunteer} />
      </Sheet>
    </>
  );
}

function MinistriesScreen({ token }: { token: string }) {
  const [items, setItems] = useState<Ministry[]>([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyMinistryForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setItems(await request<Ministry[]>("/ministries", { token }));
  }, [token]);

  useEffect(() => {
    load()
      .catch(err => setError(err instanceof Error ? err.message : "Erro ao carregar ministerios."))
      .finally(() => setLoading(false));
  }, [load]);

  async function saveMinistry() {
    setSaving(true);
    try {
      await request<Ministry>("/ministries", {
        token,
        method: "POST",
        body: {
          name: form.name.trim(),
          description: form.description.trim(),
          active: true,
        },
      });
      setForm(emptyMinistryForm);
      setFormOpen(false);
      await load();
    } catch (err) {
      Alert.alert("Nao foi possivel salvar", err instanceof Error ? err.message : "Tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingBlock label="Carregando ministerios..." />;
  }

  return (
    <>
      <FlatList
        contentContainerStyle={styles.listContent}
        data={items}
        keyExtractor={item => String(item.id)}
        ListHeaderComponent={
          <>
            <AppButton label="Novo ministerio" onPress={() => setFormOpen(true)} />
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </>
        }
        ListEmptyComponent={<EmptyState title="Nenhum ministerio" text="Crie ministerios para organizar voluntarios e escalas." />}
        renderItem={({ item }) => (
          <AppCard style={styles.scheduleCard}>
            <View style={styles.rowBetween}>
              <Text style={styles.cardTitle}>{item.name}</Text>
              <Text style={[styles.statusBadge, item.active ? styles.statusActive : styles.statusInactive]}>
                {item.active ? "Ativo" : "Inativo"}
              </Text>
            </View>
            {item.description ? <Text style={styles.noteText}>{item.description}</Text> : null}
          </AppCard>
        )}
      />

      <Sheet visible={formOpen} title="Novo ministerio" onClose={() => setFormOpen(false)}>
        <TextField label="Nome" value={form.name} onChangeText={name => setForm({ ...form, name })} />
        <TextField label="Descricao" value={form.description} onChangeText={description => setForm({ ...form, description })} multiline />
        <AppButton label="Salvar ministerio" loading={saving} disabled={!form.name.trim()} onPress={saveMinistry} />
      </Sheet>
    </>
  );
}

function AdminScreen({ token }: { token: string }) {
  const [volunteers, setVolunteers] = useState<Volunteer[]>([]);
  const [ministries, setMinistries] = useState<Ministry[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    const [volunteerData, ministryData, scheduleData, conflictData] = await Promise.all([
      request<Volunteer[]>("/volunteers", { token }),
      request<Ministry[]>("/ministries", { token }),
      request<Schedule[]>("/schedules", { token }),
      request<Conflict[]>("/schedules/conflicts", { token }),
    ]);
    setVolunteers(volunteerData);
    setMinistries(ministryData);
    setSchedules(scheduleData);
    setConflicts(conflictData);
  }, [token]);

  useEffect(() => {
    load()
      .catch(err => setError(err instanceof Error ? err.message : "Erro ao carregar painel admin."))
      .finally(() => setLoading(false));
  }, [load]);

  if (loading) {
    return <LoadingBlock label="Carregando painel..." />;
  }

  return (
    <ScrollView contentContainerStyle={styles.listContent}>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
      <View style={styles.metricGrid}>
        <MetricCard label="Voluntarios" value={volunteers.length} />
        <MetricCard label="Ministerios" value={ministries.length} />
        <MetricCard label="Escalas" value={schedules.length} />
      </View>
      <MetricCard label="Conflitos" value={conflicts.length} danger={conflicts.length > 0} />

      <Text style={styles.sectionTitle}>Conflitos</Text>
      {conflicts.length === 0 ? (
        <EmptyState title="Sem conflitos" text="As escalas atuais nao possuem alertas." />
      ) : (
        conflicts.map(conflict => (
          <AppCard key={conflict.scheduleId} style={styles.scheduleCard}>
            <Text style={styles.cardTitle}>{conflict.volunteerName}</Text>
            <Text style={styles.cardMeta}>{conflict.ministryName}</Text>
            <Text style={styles.errorText}>{conflict.message}</Text>
          </AppCard>
        ))
      )}
    </ScrollView>
  );
}

function MetricCard({ label, value, danger }: { label: string; value: number; danger?: boolean }) {
  return (
    <AppCard style={styles.metricCard}>
      <Text style={[styles.metricValue, danger && styles.metricDanger]}>{value}</Text>
      <Text style={styles.metricLabel}>{label}</Text>
    </AppCard>
  );
}

function LoadingBlock({ label }: { label: string }) {
  return (
    <View style={styles.centerScreen}>
      <ActivityIndicator color={colors.primary} />
      <Text style={styles.loadingText}>{label}</Text>
    </View>
  );
}

function Segment({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[styles.segment, active && styles.segmentActive]}>
      <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{label}</Text>
    </Pressable>
  );
}

function OptionGrid({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: Array<{ label: string; value: string }>;
  onChange: (value: string) => void;
}) {
  return (
    <View style={styles.optionBlock}>
      <Text style={styles.optionLabel}>{label}</Text>
      <View style={styles.optionGrid}>
        {options.length === 0 ? <Text style={styles.mutedText}>Nenhuma opcao disponivel.</Text> : null}
        {options.map(option => (
          <Pressable
            key={option.value}
            onPress={() => onChange(option.value)}
            style={[styles.optionChip, value === option.value && styles.optionChipActive]}
          >
            <Text style={[styles.optionText, value === option.value && styles.optionTextActive]} numberOfLines={1}>
              {option.label}
            </Text>
          </Pressable>
        ))}
      </View>
    </View>
  );
}

function Sheet({
  visible,
  title,
  children,
  onClose,
}: {
  visible: boolean;
  title: string;
  children: React.ReactNode;
  onClose: () => void;
}) {
  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <View style={styles.sheet}>
          <View style={styles.rowBetween}>
            <Text style={styles.sheetTitle}>{title}</Text>
            <AppButton label="Fechar" variant="ghost" onPress={onClose} />
          </View>
          <ScrollView contentContainerStyle={styles.sheetContent} keyboardShouldPersistTaps="handled">
            {children}
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

function formatDate(value: string) {
  const [year, month, day] = value.split("-");
  return year && month && day ? `${day}/${month}/${year}` : value;
}

function formatTime(value: string) {
  return value?.slice(0, 5) || "--:--";
}

function normalizeTime(value: string) {
  return value.length === 5 ? `${value}:00` : value;
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  centerScreen: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.background,
    gap: spacing.sm,
  },
  loadingText: {
    color: colors.muted,
    fontSize: typography.body,
  },
  header: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
    paddingBottom: spacing.md,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.md,
  },
  headerText: {
    flex: 1,
  },
  kicker: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "700",
    textTransform: "uppercase",
  },
  title: {
    color: colors.text,
    fontSize: typography.title,
    fontWeight: "800",
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.body,
  },
  tabs: {
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.sm,
    gap: spacing.sm,
  },
  tabButton: {
    minHeight: 42,
    minWidth: 110,
    paddingHorizontal: spacing.md,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: radius.md,
    backgroundColor: colors.surfaceAlt,
  },
  tabButtonActive: {
    backgroundColor: colors.surface,
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 8,
    elevation: 2,
  },
  tabText: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  tabTextActive: {
    color: colors.text,
  },
  loginScreen: {
    flex: 1,
    backgroundColor: colors.primary,
  },
  loginContent: {
    flexGrow: 1,
    justifyContent: "center",
    padding: spacing.lg,
  },
  loginHero: {
    marginBottom: spacing.xl,
  },
  loginKicker: {
    color: colors.gold,
    fontSize: typography.caption,
    fontWeight: "800",
    textTransform: "uppercase",
  },
  loginTitle: {
    color: colors.onPrimary,
    fontSize: 34,
    fontWeight: "900",
    lineHeight: 39,
    marginTop: spacing.xs,
  },
  loginText: {
    color: colors.onPrimaryMuted,
    fontSize: typography.body,
    lineHeight: 23,
    marginTop: spacing.sm,
  },
  loginCard: {
    gap: spacing.md,
  },
  switchRow: {
    flexDirection: "row",
    padding: 4,
    borderRadius: radius.md,
    backgroundColor: colors.surfaceAlt,
    gap: 4,
  },
  segment: {
    flex: 1,
    minHeight: 40,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: radius.sm,
  },
  segmentActive: {
    backgroundColor: colors.surface,
  },
  segmentText: {
    color: colors.muted,
    fontWeight: "800",
    fontSize: typography.caption,
  },
  segmentTextActive: {
    color: colors.text,
  },
  listContent: {
    padding: spacing.lg,
    paddingBottom: spacing.xxl,
    gap: spacing.md,
  },
  scheduleCard: {
    gap: spacing.xs,
  },
  availabilityCard: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.md,
  },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.md,
  },
  flexOne: {
    flex: 1,
  },
  cardTitle: {
    flex: 1,
    color: colors.text,
    fontSize: typography.subtitle,
    fontWeight: "800",
  },
  cardMeta: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  cardText: {
    color: colors.text,
    fontSize: typography.body,
  },
  noteText: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 22,
    marginTop: spacing.xs,
  },
  mutedText: {
    color: colors.muted,
    fontSize: typography.body,
  },
  inlineLoading: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
  },
  warningBox: {
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.md,
    backgroundColor: colors.primarySoft,
  },
  warningText: {
    color: colors.primaryDark,
    fontSize: typography.body,
    fontWeight: "700",
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.body,
    fontWeight: "700",
  },
  conflictBadge: {
    color: colors.danger,
    backgroundColor: colors.dangerSoft,
    borderRadius: radius.full,
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  statusBadge: {
    borderRadius: radius.full,
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  statusActive: {
    color: "#166534",
    backgroundColor: "#DCFCE7",
  },
  statusInactive: {
    color: colors.muted,
    backgroundColor: colors.surfaceAlt,
  },
  metricGrid: {
    flexDirection: "row",
    gap: spacing.sm,
  },
  metricCard: {
    flex: 1,
    alignItems: "center",
    gap: 2,
  },
  metricValue: {
    color: colors.text,
    fontSize: 26,
    fontWeight: "900",
  },
  metricDanger: {
    color: colors.danger,
  },
  metricLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  sectionTitle: {
    color: colors.text,
    fontSize: typography.subtitle,
    fontWeight: "900",
    marginTop: spacing.sm,
  },
  optionBlock: {
    gap: spacing.xs,
  },
  optionLabel: {
    color: colors.text,
    fontSize: typography.caption,
    fontWeight: "800",
    textTransform: "uppercase",
  },
  optionGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  optionChip: {
    maxWidth: "100%",
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    borderRadius: radius.full,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  optionChipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.primarySoft,
  },
  optionText: {
    color: colors.text,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  optionTextActive: {
    color: colors.primaryDark,
  },
  modalBackdrop: {
    flex: 1,
    justifyContent: "flex-end",
    backgroundColor: "rgba(0,0,0,0.35)",
  },
  sheet: {
    maxHeight: "88%",
    backgroundColor: colors.background,
    borderTopLeftRadius: radius.lg,
    borderTopRightRadius: radius.lg,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
  },
  sheetTitle: {
    flex: 1,
    color: colors.text,
    fontSize: typography.subtitle,
    fontWeight: "900",
  },
  sheetContent: {
    paddingVertical: spacing.lg,
    gap: spacing.md,
  },
});
