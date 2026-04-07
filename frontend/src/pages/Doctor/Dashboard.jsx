import { useContext, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { getAppointments, getDoctors } from "../../services/api";
import "./QueueConsole.css";

const getToday = () => {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().split("T")[0];
};

function DoctorDashboard() {
  const { user } = useContext(AuthContext);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorName, setSelectedDoctorName] = useState("");
  const [selectedDate, setSelectedDate] = useState(getToday());
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    const loadDoctors = async () => {
      try {
        const doctorList = await getDoctors();
        if (ignore) {
          return;
        }

        setDoctors(doctorList);

        const preferredDoctor =
          doctorList.find((doctor) => doctor.doctorName === user?.name)?.doctorName ||
          doctorList[0]?.doctorName ||
          "";

        setSelectedDoctorName((current) => current || preferredDoctor);
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load doctors.");
          setLoading(false);
        }
      }
    };

    loadDoctors();
    return () => {
      ignore = true;
    };
  }, [user]);

  useEffect(() => {
    let ignore = false;

    const loadAppointments = async () => {
      if (!selectedDoctorName) {
        setAppointments([]);
        setLoading(false);
        return;
      }

      setLoading(true);

      try {
        const queue = await getAppointments({
          doctorName: selectedDoctorName,
          appointmentDate: selectedDate,
        });

        if (!ignore) {
          setAppointments(queue);
          setMessage("");
        }
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load doctor queue.");
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadAppointments();
    return () => {
      ignore = true;
    };
  }, [selectedDate, selectedDoctorName]);

  const selectedDoctor = useMemo(
    () => doctors.find((doctor) => doctor.doctorName === selectedDoctorName),
    [doctors, selectedDoctorName]
  );

  const currentPatient =
    appointments.find((appointment) => appointment.status === "Now calling") || appointments[0];
  const completedCount = appointments.filter(
    (appointment) => appointment.status === "Completed"
  ).length;
  const waitingCount = appointments.filter(
    (appointment) => appointment.status !== "Completed"
  ).length;
  const nextPatients = appointments
    .filter((appointment) => appointment.id !== currentPatient?.id)
    .slice(0, 3);

  return (
    <section className="queue-console">
      <div className="queue-console__hero">
        <div>
          <p className="queue-console__eyebrow">Doctor Dashboard</p>
          <h1 className="queue-console__title">Live queue overview for doctors</h1>
          <p className="queue-console__copy">
            Pick a doctor and date to see the active patient, upcoming queue, and visit
            load without relying on dummy cards.
          </p>
        </div>

        <div className="queue-console__actions">
          <Link className="queue-console__button" to="/doctor/manage-queue">
            Open queue manager
          </Link>
          <Link className="queue-console__button queue-console__button--ghost" to="/patient/book">
            View patient booking
          </Link>
        </div>
      </div>

      <div className="queue-toolbar">
        <label className="queue-field">
          <span className="queue-field__label">Doctor</span>
          <select
            className="queue-field__control"
            value={selectedDoctorName}
            onChange={(event) => setSelectedDoctorName(event.target.value)}
          >
            {doctors.map((doctor) => (
              <option key={doctor.doctorId} value={doctor.doctorName}>
                {doctor.doctorName} - {doctor.specialization}
              </option>
            ))}
          </select>
        </label>

        <label className="queue-field">
          <span className="queue-field__label">Date</span>
          <input
            className="queue-field__control"
            type="date"
            value={selectedDate}
            onChange={(event) => setSelectedDate(event.target.value)}
          />
        </label>
      </div>

      {message && <p className="queue-banner">{message}</p>}

      <div className="queue-stats">
        <article className="queue-stat">
          <span className="queue-stat__label">Selected doctor</span>
          <strong className="queue-stat__value">
            {selectedDoctor ? selectedDoctor.doctorName : loading ? "..." : "No doctor"}
          </strong>
          <small className="queue-stat__note">
            {selectedDoctor
              ? `${selectedDoctor.specialization} • ${selectedDoctor.clinic || "Clinic pending"}`
              : "Choose a doctor to begin."}
          </small>
        </article>

        <article className="queue-stat">
          <span className="queue-stat__label">Pending patients</span>
          <strong className="queue-stat__value">{loading ? "..." : waitingCount}</strong>
          <small className="queue-stat__note">Patients still in today&apos;s queue.</small>
        </article>

        <article className="queue-stat">
          <span className="queue-stat__label">Completed visits</span>
          <strong className="queue-stat__value">{loading ? "..." : completedCount}</strong>
          <small className="queue-stat__note">Appointments marked completed for this date.</small>
        </article>

        <article className="queue-stat">
          <span className="queue-stat__label">Current patient</span>
          <strong className="queue-stat__value">
            {loading ? "..." : currentPatient?.userName || "No active patient"}
          </strong>
          <small className="queue-stat__note">
            {currentPatient
              ? `${currentPatient.appointmentTime} • Token #${currentPatient.tokenNumber}`
              : "No patient is in the queue yet."}
          </small>
        </article>
      </div>

      <div className="queue-grid">
        <section className="queue-panel">
          <div className="queue-panel__header">
            <div>
              <p className="queue-console__eyebrow">Now Calling</p>
              <h2 className="queue-panel__title">Current patient focus</h2>
            </div>
          </div>

          {loading ? (
            <div className="queue-empty">Loading queue details...</div>
          ) : currentPatient ? (
            <article className="queue-patient queue-patient--active">
              <div>
                <strong>{currentPatient.userName}</strong>
                <p>
                  {currentPatient.specialization} • Token #{currentPatient.tokenNumber}
                </p>
              </div>
              <div className="queue-patient__meta">
                <span>{currentPatient.appointmentDate}</span>
                <span>{currentPatient.appointmentTime}</span>
                <span>{currentPatient.status}</span>
              </div>
            </article>
          ) : (
            <div className="queue-empty">
              No appointments found for this doctor on the selected date.
            </div>
          )}
        </section>

        <section className="queue-panel">
          <div className="queue-panel__header">
            <div>
              <p className="queue-console__eyebrow">Up Next</p>
              <h2 className="queue-panel__title">Upcoming patients</h2>
            </div>
          </div>

          {loading ? (
            <div className="queue-empty">Loading upcoming patients...</div>
          ) : nextPatients.length === 0 ? (
            <div className="queue-empty">No additional patients are waiting right now.</div>
          ) : (
            <div className="queue-list">
              {nextPatients.map((appointment) => (
                <article key={appointment.id} className="queue-patient">
                  <div>
                    <strong>{appointment.userName}</strong>
                    <p>Token #{appointment.tokenNumber}</p>
                  </div>
                  <div className="queue-patient__meta">
                    <span>{appointment.appointmentTime}</span>
                    <span>{appointment.status}</span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </section>
  );
}

export default DoctorDashboard;
