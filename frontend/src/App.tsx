import { useEffect, useState } from "react";
import { LoginPanel } from "./components/LoginPanel";
import type { LoginMode } from "./components/LoginPanel";

function PresentationScreen({ onStart }: { onStart: (mode: LoginMode) => void }) {
  return (
    <main id="telaApresentacao" className="presentation-page">
      <section className="presentation-shell">
        <div className="presentation-brand">
          <img src="img/logo-shekinah.png" alt="Shekinah" className="presentation-logo" />
          <span>Sistema Shekinah</span>
        </div>

        <div className="presentation-content">
          <p className="presentation-kicker">Escalas ministeriais online</p>
          <h1>Organize as escalas da igreja com clareza e rapidez.</h1>
          <p className="presentation-description">
            O Shekinah ajuda administradores e voluntarios a acompanhar ministerios,
            disponibilidade, conflitos e escalas em um so lugar.
          </p>

          <div className="presentation-actions">
            <button className="presentation-primary" type="button" onClick={() => onStart("entrar")}>
              Entrar
            </button>
            <button className="presentation-secondary" type="button" onClick={() => onStart("cadastro")}>
              Criar conta
            </button>
          </div>
        </div>

        <div className="presentation-preview" aria-label="Resumo de funcionamento do sistema">
          <div className="presentation-preview-header">
            <span>Como funciona</span>
            <strong>3 passos</strong>
          </div>
          <div className="presentation-step">
            <span>1</span>
            <div>
              <strong>Cadastre ministerios e voluntarios</strong>
              <p>Mantenha cada equipe organizada e pronta para montar escalas.</p>
            </div>
          </div>
          <div className="presentation-step">
            <span>2</span>
            <div>
              <strong>Informe disponibilidade</strong>
              <p>Voluntarios indicam dias livres e indisponibilidades.</p>
            </div>
          </div>
          <div className="presentation-step">
            <span>3</span>
            <div>
              <strong>Acompanhe tudo online</strong>
              <p>Escalas, conflitos e pedidos ficam visiveis no painel.</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export function App() {
  const [showPresentation, setShowPresentation] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [loginMode, setLoginMode] = useState<LoginMode>("entrar");

  useEffect(() => {
    function hidePublicScreens() {
      setAuthenticated(true);
      setShowPresentation(false);
    }

    function showPresentationAgain() {
      setAuthenticated(false);
      setLoginMode("entrar");
      setShowPresentation(true);
    }

    window.addEventListener("shekinah:authenticated", hidePublicScreens);
    window.addEventListener("shekinah:logout", showPresentationAgain);
    window.inicializarInterface();

    return () => {
      window.removeEventListener("shekinah:authenticated", hidePublicScreens);
      window.removeEventListener("shekinah:logout", showPresentationAgain);
    };
  }, []);

  function handleStart(mode: LoginMode) {
    setAuthenticated(false);
    setLoginMode(mode);
    setShowPresentation(false);
  }

  return (
    <>
      <button
        id="themeToggleBtn"
        className="theme-toggle"
        type="button"
        onClick={() => window.toggleTheme()}
        aria-label="Ativar tema escuro"
        title="Ativar tema escuro"
      >
        <span className="theme-toggle-label" id="themeToggleLabel">Tema escuro</span>
      </button>

      {showPresentation && <PresentationScreen onStart={handleStart} />}

      <LoginPanel initialMode={loginMode} hidden={showPresentation || authenticated} />

      <div id="appContainer" className="hidden">
        <header className="app-header">
          <button
            id="mobileMenuToggle"
            className="mobile-menu-toggle"
            type="button"
            onClick={() => window.toggleMobileMenu()}
            aria-label="Abrir menu"
            title="Abrir menu"
          >
            <span />
            <span />
            <span />
          </button>
          <div className="header-brand">
            <img src="img/logo-shekinah.png" alt="Logo Shekinah" className="header-logo" />
            <div className="header-brand-text">
              <span className="header-brand-name">Shekinah</span>
              <span className="header-brand-sub">Sistema de Escalas</span>
            </div>
            <span className="header-badge" id="headerBadge">Admin</span>
          </div>
          <div className="header-user">
            <span id="headerNome">Administrador</span>
          </div>
        </header>

        <div className="app-layout">
          <div id="mobileNavBackdrop" className="mobile-nav-backdrop hidden" onClick={() => window.fecharMenuMobile()} />
          <aside className="sidebar" id="sidebar" />
          <main className="main-content" id="mainContent" />
        </div>
      </div>

      <div className="modal-overlay hidden" id="modalOverlay" onClick={event => window.fecharModal(event)}>
        <div className="modal" id="modalContent" />
      </div>

      <div id="toastContainer" />
    </>
  );
}
