import { useState } from "react";
import { ToastContext } from "./toast-context";
import "../components/Toast.css";

const TOAST_LIFETIME_MS = 3200;

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = (id) => {
    setToasts((currentToasts) => currentToasts.filter((toast) => toast.id !== id));
  };

  const showToast = (message, variant = "success") => {
    if (!message) {
      return;
    }

    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
    setToasts((currentToasts) => [...currentToasts, { id, message, variant }]);

    window.setTimeout(() => {
      removeToast(id);
    }, TOAST_LIFETIME_MS);
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}

      <div className="toast-stack" aria-live="polite" aria-atomic="true">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={
              toast.variant === "error"
                ? "toast toast--error"
                : "toast toast--success"
            }
            role="status"
          >
            <div className="toast__icon">{toast.variant === "error" ? "!" : "OK"}</div>
            <div className="toast__content">
              <strong className="toast__title">
                {toast.variant === "error" ? "Action failed" : "Success"}
              </strong>
              <p className="toast__message">{toast.message}</p>
            </div>
            <button
              className="toast__close"
              type="button"
              onClick={() => removeToast(toast.id)}
              aria-label="Close notification"
            >
              x
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};
