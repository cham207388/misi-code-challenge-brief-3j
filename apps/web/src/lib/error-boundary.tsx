import { Component, type ErrorInfo, type ReactNode } from "react";
import { ErrorBanner } from "./errors";

type Props = { children: ReactNode };
type State = { error: unknown };

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: unknown): State {
    return { error };
  }

  componentDidCatch(error: unknown, info: ErrorInfo) {
    console.error(error, info);
  }

  render() {
    if (this.state.error) {
      return (
        <main id="main" className="px-6 py-16">
          <h1 className="font-serif text-3xl text-forest-deep">Something went wrong</h1>
          <ErrorBanner error={this.state.error} fallback="The page failed to render" />
          <button type="button" className="mt-4 text-forest underline" onClick={() => this.setState({ error: null })}>
            Try again
          </button>
        </main>
      );
    }
    return this.props.children;
  }
}
