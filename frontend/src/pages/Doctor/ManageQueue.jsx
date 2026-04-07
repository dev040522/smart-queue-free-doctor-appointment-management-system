import { useContext, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { getAppointments, getDoctors, updateAppointmentStatus } from "../../services/api";
import "./QueueConsole.css";

const getToday = () => {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().split("T")[0];
};

function ManageQueue() {
  const { user } = useContext(AuthContext);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorName, setSelectedDoctorName] = useState("");
  const [selectedDate, setSelectedDate] = useState(getToday());
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [updatingId, setUpdatingId] = useState(null);

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
          setMessage(error.response?.data?.message || "Unable to load queue data.");
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

  const handleStatusUpdate = async (appointmentId, status) => {
    setUpdatingId(appointmentId);

    try {
      const updatedAppointment = await updateAppointmentStatus(appointmentId, status);
      setMessage(
        `${updatedAppointment.userName} is now marked as ${updatedAppointment.status}.`
      );

      const refreshedQueue = await getAppointments({
        doctorName: selectedDoctorName,
        appointmentDate: selectedDate,
      });
      setAppointments(refreshedQueue);
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to update appointment status.");
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <section className="queue-console">
      <div className="queue-console__hero">
        <div>
          <p className="queue-console__eyebrow">Queue Manager</p>
          <h1 className="queue-console__title">Call, complete, and control the doctor queue</h1>
          <p className="queue-console__copy">
            This screen is now connected to live appointment data so doctors can manage
            the day&apos;s queue instead of clicking through placeholder cards.
          </p>
        </div>

        <div className="queue-console__actions">
          <Link className="queue-console__button" to="/doctor/dashboard">
            Back to dashboard
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

      {selectedDoctor && (
        <div className="queue-banner">
          Managing <strong>{selectedDoctor.doctorName}</strong> in {selectedDoctor.specialization}
          {selectedDoctor.clinic ? ` at ${selectedDoctor.clinic}` : ""}.
        </div>
      )}

      {message && <p className="queue-banner">{message}</p>}

      <section className="queue-panel">
        <div className="queue-panel__header">
          <div>
            <p className="queue-console__eyebrow">Live Queue</p>
            <h2 className="queue-panel__title">Appointments for the selected doctor</h2>
          </div>
        </div>

        {loading ? (
          <div className="queue-empty">Loading appointments...</div>
        ) : appointments.length === 0 ? (
          <div className="queue-empty">
            No appointments are booked for this doctor on the selected date yet.
          </div>
        ) : (
          <div className="queue-list">
            {appointments.map((appointment) => (
              <article
                key={appointment.id}
                className={
                  appointment.status === "Now calling"
                    ? "queue-patient queue-patient--active"
                    : "queue-patient"
                }
              >
                <div>
                  <strong>{appointment.userName}</strong>
                  <p>
                    {appointment.appointmentTime} • Token #{appointment.tokenNumber}
                  </p>
                  <p>{appointment.symptoms || "No symptoms added."}</p>
                </div>

                <div className="queue-patient__side">
                  <span className="queue-patient__status">{appointment.status}</span>
                  <div className="queue-actions">
                    <button
                      className="queue-actions__button"
                      type="button"
                      disabled={updatingId === appointment.id || appointment.status === "Now calling"}
                      onClick={() => handleStatusUpdate(appointment.id, "Now calling")}
                    >
                      {updatingId === appointment.id ? "Saving..." : "Call now"}
                    </button>
                    <button
                      className="queue-actions__button queue-actions__button--ghost"
                      type="button"
                      disabled={updatingId === appointment.id || appointment.status === "Completed"}
                      onClick={() => handleStatusUpdate(appointment.id, "Completed")}
                    >
                      Complete
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}

export default ManageQueue;
