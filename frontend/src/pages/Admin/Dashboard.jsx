import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getAllAppointments, getDoctors, getUsers } from "../../services/api";
import "./ManageUsers.css";

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    const loadOverview = async () => {
      try {
        const [usersData, doctorsData, appointmentsData] = await Promise.all([
          getUsers(),
          getDoctors(),
          getAllAppointments(),
        ]);

        if (!ignore) {
          setUsers(usersData);
          setDoctors(doctorsData);
          setAppointments(appointmentsData);
          setMessage("");
        }
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load live admin data.");
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadOverview();
    return () => {
      ignore = true;
    };
  }, []);

  const totalPatients = users.filter((user) => user.role === "patient").length;
  const totalRegisteredDoctors = users.filter((user) => user.role === "doctor").length;
  const totalUsers = users.length;
  const recentAppointments = appointments.slice(0, 5);

  return (
    <section className="admin-console">
      <div className="admin-console__hero">
        <div>
          <p className="admin-console__eyebrow">Admin Overview</p>
          <h1 className="admin-console__title">Live hospital database dashboard</h1>
          <p className="admin-console__copy">
            Monitor registered users, available doctors, and booked appointments from the
            backend in one place.
          </p>
        </div>

        <div className="admin-console__actions">
          <Link className="admin-console__button" to="/admin/database">
            Open database center
          </Link>
          <Link className="admin-console__button admin-console__button--ghost" to="/patient/book">
            View patient booking
          </Link>
        </div>
      </div>

      {message && <p className="admin-console__message">{message}</p>}

      <div className="admin-stats">
        <article className="admin-stat">
          <span className="admin-stat__label">Total users</span>
          <strong className="admin-stat__value">{loading ? "..." : totalUsers}</strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Patients</span>
          <strong className="admin-stat__value">{loading ? "..." : totalPatients}</strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Doctor accounts</span>
          <strong className="admin-stat__value">
            {loading ? "..." : totalRegisteredDoctors}
          </strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Doctor master list</span>
          <strong className="admin-stat__value">{loading ? "..." : doctors.length}</strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Appointments</span>
          <strong className="admin-stat__value">{loading ? "..." : appointments.length}</strong>
        </article>
      </div>

      <div className="admin-surface">
        <div className="admin-surface__header">
          <div>
            <p className="admin-console__eyebrow">Latest appointments</p>
            <h2 className="admin-surface__title">Recent booking activity</h2>
          </div>
          <Link className="admin-inline-link" to="/admin/database">
            See full database
          </Link>
        </div>

        {loading ? (
          <div className="admin-empty">Loading live records from the backend...</div>
        ) : recentAppointments.length === 0 ? (
          <div className="admin-empty">No appointments have been booked yet.</div>
        ) : (
          <div className="admin-list">
            {recentAppointments.map((appointment) => (
              <article key={appointment.id} className="admin-list__item">
                <div>
                  <strong>{appointment.userName}</strong>
                  <p>
                    {appointment.specialization} with {appointment.doctorName}
                  </p>
                </div>
                <div className="admin-list__meta">
                  <span>
                    {appointment.appointmentDate} at {appointment.appointmentTime}
                  </span>
                  <span>Token #{appointment.tokenNumber}</span>
                  <span>{appointment.status}</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default AdminDashboard;
