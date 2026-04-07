import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  deleteAdminAppointment,
  deleteAdminDoctor,
  deleteAdminUser,
  getAllAppointments,
  getDoctors,
  getUsers,
  updateAdminDoctor,
  updateAdminUser,
  updateAppointmentStatus,
} from "../../services/api";
import "./ManageUsers.css";

const STATUS_OPTIONS = ["Waiting", "Up next", "Now calling", "Completed", "Cancelled"];

const formatRole = (role) => {
  if (!role) {
    return "Unknown";
  }

  return role.charAt(0).toUpperCase() + role.slice(1);
};

const buildUserDrafts = (users) =>
  Object.fromEntries(
    users.map((user) => [
      user.userId,
      {
        name: user.name ?? "",
        email: user.email ?? "",
        phoneNumber: user.phoneNumber ?? "",
      },
    ])
  );

const buildDoctorDrafts = (doctors) =>
  Object.fromEntries(
    doctors.map((doctor) => [
      doctor.doctorId,
      {
        doctorName: doctor.doctorName ?? "",
        specialization: doctor.specialization ?? "",
        clinic: doctor.clinic ?? "",
        experience: doctor.experience ?? "",
        availableFrom: doctor.availableFrom ?? "",
        availableTo: doctor.availableTo ?? "",
      },
    ])
  );

const buildAppointmentDrafts = (appointments) =>
  Object.fromEntries(
    appointments.map((appointment) => [
      appointment.id,
      {
        status: appointment.status ?? "Waiting",
      },
    ])
  );

const fetchAdminSnapshot = async () => {
  const [users, doctors, appointments] = await Promise.all([
    getUsers(),
    getDoctors(),
    getAllAppointments(),
  ]);

  return { users, doctors, appointments };
};

const getErrorMessage = (error, fallback) =>
  error.response?.data?.message || error.response?.data?.error || fallback;

function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [userDrafts, setUserDrafts] = useState({});
  const [doctorDrafts, setDoctorDrafts] = useState({});
  const [appointmentDrafts, setAppointmentDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("info");
  const [savingKey, setSavingKey] = useState("");
  const [deletingKey, setDeletingKey] = useState("");

  useEffect(() => {
    let ignore = false;

    const loadDatabase = async () => {
      setLoading(true);

      try {
        const snapshot = await fetchAdminSnapshot();

        if (ignore) {
          return;
        }

        setUsers(snapshot.users);
        setDoctors(snapshot.doctors);
        setAppointments(snapshot.appointments);
        setUserDrafts(buildUserDrafts(snapshot.users));
        setDoctorDrafts(buildDoctorDrafts(snapshot.doctors));
        setAppointmentDrafts(buildAppointmentDrafts(snapshot.appointments));
        setMessage("");
        setMessageType("info");
      } catch (error) {
        if (!ignore) {
          setMessage(getErrorMessage(error, "Unable to load database records."));
          setMessageType("error");
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadDatabase();
    return () => {
      ignore = true;
    };
  }, []);

  const refreshDatabase = async (successMessage = "") => {
    setLoading(true);

    try {
      const snapshot = await fetchAdminSnapshot();
      setUsers(snapshot.users);
      setDoctors(snapshot.doctors);
      setAppointments(snapshot.appointments);
      setUserDrafts(buildUserDrafts(snapshot.users));
      setDoctorDrafts(buildDoctorDrafts(snapshot.doctors));
      setAppointmentDrafts(buildAppointmentDrafts(snapshot.appointments));
      setMessage(successMessage);
      setMessageType(successMessage ? "success" : "info");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to refresh database records."));
      setMessageType("error");
    } finally {
      setLoading(false);
    }
  };

  const isRowBusy = (rowKey) => loading || savingKey === rowKey || deletingKey === rowKey;

  const hasUserChanges = (user) => {
    const draft = userDrafts[user.userId];
    if (!draft) {
      return false;
    }

    return (
      draft.name !== (user.name ?? "") ||
      draft.email !== (user.email ?? "") ||
      draft.phoneNumber !== (user.phoneNumber ?? "")
    );
  };

  const hasDoctorChanges = (doctor) => {
    const draft = doctorDrafts[doctor.doctorId];
    if (!draft) {
      return false;
    }

    return (
      draft.doctorName !== (doctor.doctorName ?? "") ||
      draft.specialization !== (doctor.specialization ?? "") ||
      draft.clinic !== (doctor.clinic ?? "") ||
      draft.experience !== (doctor.experience ?? "") ||
      draft.availableFrom !== (doctor.availableFrom ?? "") ||
      draft.availableTo !== (doctor.availableTo ?? "")
    );
  };

  const hasAppointmentChanges = (appointment) => {
    const draft = appointmentDrafts[appointment.id];
    return draft ? draft.status !== (appointment.status ?? "Waiting") : false;
  };

  const handleUserDraftChange = (userId, field, value) => {
    setUserDrafts((current) => ({
      ...current,
      [userId]: {
        ...current[userId],
        [field]: value,
      },
    }));
  };

  const handleDoctorDraftChange = (doctorId, field, value) => {
    setDoctorDrafts((current) => ({
      ...current,
      [doctorId]: {
        ...current[doctorId],
        [field]: value,
      },
    }));
  };

  const handleAppointmentDraftChange = (appointmentId, value) => {
    setAppointmentDrafts((current) => ({
      ...current,
      [appointmentId]: {
        ...current[appointmentId],
        status: value,
      },
    }));
  };

  const handleUserSave = async (user) => {
    const rowKey = `user-save-${user.userId}`;
    setSavingKey(rowKey);

    try {
      const response = await updateAdminUser(user.userId, userDrafts[user.userId]);
      await refreshDatabase(response.message || "User updated successfully.");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to update user."));
      setMessageType("error");
    } finally {
      setSavingKey("");
    }
  };

  const handleDeleteUser = async (user) => {
    if (!window.confirm(`Delete ${user.name || user.email || "this user"}?`)) {
      return;
    }

    const rowKey = `user-delete-${user.userId}`;
    setDeletingKey(rowKey);

    try {
      const response = await deleteAdminUser(user.userId);
      await refreshDatabase(response.message || "User deleted successfully.");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to delete user."));
      setMessageType("error");
    } finally {
      setDeletingKey("");
    }
  };

  const handleDoctorSave = async (doctor) => {
    const rowKey = `doctor-save-${doctor.doctorId}`;
    setSavingKey(rowKey);

    try {
      const response = await updateAdminDoctor(doctor.doctorId, doctorDrafts[doctor.doctorId]);
      await refreshDatabase(response.message || "Doctor updated successfully.");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to update doctor."));
      setMessageType("error");
    } finally {
      setSavingKey("");
    }
  };

  const handleDeleteDoctor = async (doctor) => {
    if (!window.confirm(`Delete ${doctor.doctorName}?`)) {
      return;
    }

    const rowKey = `doctor-delete-${doctor.doctorId}`;
    setDeletingKey(rowKey);

    try {
      const response = await deleteAdminDoctor(doctor.doctorId);
      await refreshDatabase(response.message || "Doctor deleted successfully.");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to delete doctor."));
      setMessageType("error");
    } finally {
      setDeletingKey("");
    }
  };

  const handleAppointmentSave = async (appointment) => {
    const rowKey = `appointment-save-${appointment.id}`;
    setSavingKey(rowKey);

    try {
      await updateAppointmentStatus(
        appointment.id,
        appointmentDrafts[appointment.id]?.status || appointment.status
      );
      await refreshDatabase(`Appointment #${appointment.id} updated successfully.`);
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to update appointment."));
      setMessageType("error");
    } finally {
      setSavingKey("");
    }
  };

  const handleDeleteAppointment = async (appointment) => {
    if (!window.confirm(`Delete appointment #${appointment.id}?`)) {
      return;
    }

    const rowKey = `appointment-delete-${appointment.id}`;
    setDeletingKey(rowKey);

    try {
      const response = await deleteAdminAppointment(appointment.id);
      await refreshDatabase(response.message || "Appointment deleted successfully.");
    } catch (error) {
      setMessage(getErrorMessage(error, "Unable to delete appointment."));
      setMessageType("error");
    } finally {
      setDeletingKey("");
    }
  };

  const patientAccounts = users.filter((user) => user.role === "patient");
  const doctorAccounts = users.filter((user) => user.role === "doctor");
  const adminAccounts = users.filter((user) => user.role === "admin");

  return (
    <section className="admin-console">
      <div className="admin-console__hero">
        <div>
          <p className="admin-console__eyebrow">Database Center</p>
          <h1 className="admin-console__title">Edit and clean up live backend data</h1>
          <p className="admin-console__copy">
            Update account details, adjust doctor schedules, manage appointment status,
            and remove records without opening raw JSON endpoints.
          </p>
        </div>

        <div className="admin-console__actions">
          <button
            className="admin-console__button"
            type="button"
            onClick={() => refreshDatabase()}
            disabled={loading}
          >
            {loading ? "Refreshing..." : "Refresh data"}
          </button>
          <Link className="admin-console__button admin-console__button--ghost" to="/admin/dashboard">
            Back to dashboard
          </Link>
        </div>
      </div>

      {message && (
        <p className={`admin-console__message admin-console__message--${messageType}`}>
          {message}
        </p>
      )}

      <div className="admin-stats">
        <article className="admin-stat">
          <span className="admin-stat__label">Registered accounts</span>
          <strong className="admin-stat__value">{loading ? "..." : users.length}</strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Patient accounts</span>
          <strong className="admin-stat__value">
            {loading ? "..." : patientAccounts.length}
          </strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Doctor accounts</span>
          <strong className="admin-stat__value">
            {loading ? "..." : doctorAccounts.length}
          </strong>
        </article>
        <article className="admin-stat">
          <span className="admin-stat__label">Admin accounts</span>
          <strong className="admin-stat__value">
            {loading ? "..." : adminAccounts.length}
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

      <section className="admin-surface">
        <div className="admin-surface__header">
          <div>
            <p className="admin-console__eyebrow">Table 1</p>
            <h2 className="admin-surface__title">Login accounts</h2>
          </div>
        </div>

        <div className="admin-table-wrap admin-table-wrap--appointments">
          <table className="admin-table admin-table--appointments">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Type</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" className="admin-table__empty">
                    Loading account records...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan="7" className="admin-table__empty">
                    No accounts found.
                  </td>
                </tr>
              ) : (
                users.map((user) => {
                  const draft = userDrafts[user.userId] || {};
                  const saveKey = `user-save-${user.userId}`;
                  const deleteKey = `user-delete-${user.userId}`;

                  return (
                    <tr key={user.userId}>
                      <td>{user.userId}</td>
                      <td>{formatRole(user.accountType)}</td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.name ?? ""}
                          onChange={(event) =>
                            handleUserDraftChange(user.userId, "name", event.target.value)
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="email"
                          value={draft.email ?? ""}
                          onChange={(event) =>
                            handleUserDraftChange(user.userId, "email", event.target.value)
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.phoneNumber ?? ""}
                          onChange={(event) =>
                            handleUserDraftChange(user.userId, "phoneNumber", event.target.value)
                          }
                        />
                      </td>
                      <td>{formatRole(user.role)}</td>
                      <td>
                        <div className="admin-actions">
                          <button
                            className="admin-action admin-action--primary"
                            type="button"
                            disabled={isRowBusy(saveKey) || !hasUserChanges(user)}
                            onClick={() => handleUserSave(user)}
                          >
                            {savingKey === saveKey ? "Saving..." : "Save"}
                          </button>
                          <button
                            className="admin-action admin-action--danger"
                            type="button"
                            disabled={isRowBusy(deleteKey)}
                            onClick={() => handleDeleteUser(user)}
                          >
                            {deletingKey === deleteKey ? "Deleting..." : "Delete"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="admin-surface">
        <div className="admin-surface__header">
          <div>
            <p className="admin-console__eyebrow">Table 2</p>
            <h2 className="admin-surface__title">Doctor master list</h2>
          </div>
        </div>

        <div className="admin-table-wrap admin-table-wrap--appointments">
          <table className="admin-table admin-table--appointments">
            <thead>
              <tr>
                <th>ID</th>
                <th>Doctor</th>
                <th>Specialization</th>
                <th>Clinic</th>
                <th>Experience</th>
                <th>Available From</th>
                <th>Available To</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="admin-table__empty">
                    Loading doctors...
                  </td>
                </tr>
              ) : doctors.length === 0 ? (
                <tr>
                  <td colSpan="8" className="admin-table__empty">
                    No doctors found.
                  </td>
                </tr>
              ) : (
                doctors.map((doctor) => {
                  const draft = doctorDrafts[doctor.doctorId] || {};
                  const saveKey = `doctor-save-${doctor.doctorId}`;
                  const deleteKey = `doctor-delete-${doctor.doctorId}`;

                  return (
                    <tr key={doctor.doctorId}>
                      <td>{doctor.doctorId}</td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.doctorName ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(
                              doctor.doctorId,
                              "doctorName",
                              event.target.value
                            )
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.specialization ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(
                              doctor.doctorId,
                              "specialization",
                              event.target.value
                            )
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.clinic ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(doctor.doctorId, "clinic", event.target.value)
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="text"
                          value={draft.experience ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(
                              doctor.doctorId,
                              "experience",
                              event.target.value
                            )
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="time"
                          value={draft.availableFrom ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(
                              doctor.doctorId,
                              "availableFrom",
                              event.target.value
                            )
                          }
                        />
                      </td>
                      <td>
                        <input
                          className="admin-table__control"
                          type="time"
                          value={draft.availableTo ?? ""}
                          onChange={(event) =>
                            handleDoctorDraftChange(
                              doctor.doctorId,
                              "availableTo",
                              event.target.value
                            )
                          }
                        />
                      </td>
                      <td>
                        <div className="admin-actions">
                          <button
                            className="admin-action admin-action--primary"
                            type="button"
                            disabled={isRowBusy(saveKey) || !hasDoctorChanges(doctor)}
                            onClick={() => handleDoctorSave(doctor)}
                          >
                            {savingKey === saveKey ? "Saving..." : "Save"}
                          </button>
                          <button
                            className="admin-action admin-action--danger"
                            type="button"
                            disabled={isRowBusy(deleteKey)}
                            onClick={() => handleDeleteDoctor(doctor)}
                          >
                            {deletingKey === deleteKey ? "Deleting..." : "Delete"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="admin-surface">
        <div className="admin-surface__header">
          <div>
            <p className="admin-console__eyebrow">Table 3</p>
            <h2 className="admin-surface__title">Appointments board</h2>
          </div>
        </div>

        {loading ? (
          <div className="admin-empty">Loading appointments...</div>
        ) : appointments.length === 0 ? (
          <div className="admin-empty">No appointments found.</div>
        ) : (
          <div className="admin-appointments-grid">
            {appointments.map((appointment) => {
              const draft = appointmentDrafts[appointment.id] || {};
              const saveKey = `appointment-save-${appointment.id}`;
              const deleteKey = `appointment-delete-${appointment.id}`;

              return (
                <article key={appointment.id} className="admin-appointment-card">
                  <div className="admin-appointment-card__top">
                    <div>
                      <p className="admin-appointment-card__eyebrow">
                        Appointment #{appointment.id}
                      </p>
                      <h3 className="admin-appointment-card__title">
                        {appointment.userName || "Unknown patient"}
                      </h3>
                      <p className="admin-appointment-card__subtle">
                        {appointment.userEmail || "No email provided"}
                      </p>
                    </div>
                    <div className="admin-appointment-card__token">
                      <span>Token</span>
                      <strong>{appointment.tokenNumber ?? "-"}</strong>
                    </div>
                  </div>

                  <div className="admin-appointment-card__details">
                    <div className="admin-detail">
                      <span className="admin-detail__label">Doctor</span>
                      <strong>{appointment.doctorName || "-"}</strong>
                      <span>{appointment.specialization || "-"}</span>
                    </div>
                    <div className="admin-detail">
                      <span className="admin-detail__label">Schedule</span>
                      <strong>{appointment.appointmentDate || "-"}</strong>
                      <span>{appointment.appointmentTime || "-"}</span>
                    </div>
                    <div className="admin-detail">
                      <span className="admin-detail__label">Visit</span>
                      <strong>{appointment.consultationType || "-"}</strong>
                      <span>Fee: {appointment.consultationFee ?? "-"}</span>
                    </div>
                  </div>

                  <div className="admin-appointment-card__symptoms">
                    <span className="admin-detail__label">Symptoms</span>
                    <p>{appointment.symptoms || "No symptoms added."}</p>
                  </div>

                  <div className="admin-appointment-card__footer">
                    <label className="admin-appointment-card__status">
                      <span className="admin-detail__label">Status</span>
                      <select
                        className="admin-table__control"
                        value={draft.status ?? "Waiting"}
                        onChange={(event) =>
                          handleAppointmentDraftChange(appointment.id, event.target.value)
                        }
                      >
                        {STATUS_OPTIONS.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </label>

                    <div className="admin-actions">
                      <button
                        className="admin-action admin-action--primary"
                        type="button"
                        disabled={isRowBusy(saveKey) || !hasAppointmentChanges(appointment)}
                        onClick={() => handleAppointmentSave(appointment)}
                      >
                        {savingKey === saveKey ? "Saving..." : "Save"}
                      </button>
                      <button
                        className="admin-action admin-action--danger"
                        type="button"
                        disabled={isRowBusy(deleteKey)}
                        onClick={() => handleDeleteAppointment(appointment)}
                      >
                        {deletingKey === deleteKey ? "Deleting..." : "Delete"}
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </section>
  );
}

export default ManageUsers;
