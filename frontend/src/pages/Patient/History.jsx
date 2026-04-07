import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { getAppointments } from "../../services/api";
import "./History.css";

function AppointmentHistory() {
  const { user } = useContext(AuthContext);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    const loadHistory = async () => {
      if (!user?.email) {
        setAppointments([]);
        setLoading(false);
        setMessage("Login as a patient to see your appointment history.");
        return;
      }

      try {
        const history = await getAppointments({ userEmail: user.email });
        if (!ignore) {
          setAppointments(history);
          setMessage("");
        }
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load appointment history.");
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadHistory();
    return () => {
      ignore = true;
    };
  }, [user]);

  return (
    <section className="history-page">
      <div className="history-hero">
        <div>
          <p className="history-hero__eyebrow">Patient History</p>
          <h1 className="history-hero__title">Your booked appointments in one timeline</h1>
          <p className="history-hero__copy">
            Review past and upcoming visits, doctor details, token numbers, and appointment
            status without leaving the app.
          </p>
        </div>
        <Link className="history-hero__button" to="/patient/book">
          Book another appointment
        </Link>
      </div>

      {message && <p className="history-banner">{message}</p>}

      <div className="history-grid">
        {loading ? (
          <article className="history-card history-card--empty">
            Loading your appointment history...
          </article>
        ) : appointments.length === 0 ? (
          <article className="history-card history-card--empty">
            No appointments found yet. Once you book one, it will appear here.
          </article>
        ) : (
          appointments.map((appointment) => (
            <article key={appointment.id} className="history-card">
              <div className="history-card__header">
                <div>
                  <p className="history-card__specialty">{appointment.specialization || "General"}</p>
                  <h2 className="history-card__doctor">{appointment.doctorName || "Doctor not assigned"}</h2>
                </div>
                <span className="history-card__status">{appointment.status || "Waiting"}</span>
              </div>

              <div className="history-card__meta">
                <span>{appointment.appointmentDate || "-"}</span>
                <span>{appointment.appointmentTime || "-"}</span>
                <span>Token #{appointment.tokenNumber ?? "-"}</span>
              </div>

              <dl className="history-card__details">
                <div>
                  <dt>Patient</dt>
                  <dd>{appointment.userName || "-"}</dd>
                </div>
                <div>
                  <dt>Email</dt>
                  <dd>{appointment.userEmail || "-"}</dd>
                </div>
                <div className="history-card__details--wide">
                  <dt>Symptoms</dt>
                  <dd>{appointment.symptoms || "No symptoms added."}</dd>
                </div>
              </dl>
            </article>
          ))
        )}
      </div>
    </section>
  );
}

export default AppointmentHistory;
