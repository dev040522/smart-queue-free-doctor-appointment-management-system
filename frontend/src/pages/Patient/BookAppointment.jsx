import { useContext, useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { ToastContext } from "../../context/toast-context";
import { createAppointment, getDoctors } from "../../services/api";
import "./BookAppointment.css";

const CONSULTATION_TYPES = ["Hospital Visit", "Online Review", "Home Visit"];
const SPECIALTY_PREMIUMS = {
  Cardiology: 220,
  Orthopedics: 180,
  Pediatrics: 140,
  Neurology: 210,
  Dermatology: 120,
  ENT: 110,
  Gynecology: 170,
  "General Medicine": 100,
};

function BookAppointment() {
  const location = useLocation();
  const { user } = useContext(AuthContext);
  const { showToast } = useContext(ToastContext);
  const [doctors, setDoctors] = useState([]);
  const [selectedSpecialistId, setSelectedSpecialistId] = useState("");
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedSlot, setSelectedSlot] = useState("");
  const [patientName, setPatientName] = useState(user?.name || "");
  const [patientEmail, setPatientEmail] = useState(user?.email || "");
  const [patientConcern, setPatientConcern] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const minDate = new Date().toISOString().split("T")[0];
  const searchPreset = location.state?.searchPreset;
  const [consultationType, setConsultationType] = useState(
    CONSULTATION_TYPES.includes(searchPreset?.consultation)
      ? searchPreset.consultation
      : "Hospital Visit"
  );

  useEffect(() => {
    let ignore = false;

    const loadDoctors = async () => {
      try {
        const data = await getDoctors();
        if (!ignore) {
          setDoctors(data);
        }
      } catch (error) {
        if (!ignore) {
          setMessage(
            error.response?.data?.message || "Unable to load doctors from the backend."
          );
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    };

    loadDoctors();
    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    setPatientName(user?.name || "");
    setPatientEmail(user?.email || "");
  }, [user]);

  const specialists = useMemo(() => {
    const counts = new Map();
    const summaries = new Map([
      ["Cardiology", "Heart care, ECG review, and chest pain consultations."],
      ["Orthopedics", "Bones, joints, fractures, and mobility follow-up."],
      ["Pediatrics", "Infant, child, and teen consultations."],
      ["Neurology", "Nerves, headaches, migraines, and dizziness care."],
      ["Dermatology", "Skin, allergy, and hair-related treatment."],
      ["ENT", "Ear, nose, throat, sinus, and voice-related checkups."],
      ["Gynecology", "Women's health, routine review, and preventive care."],
      ["General Medicine", "Fever, infection, diabetes, and general health review."],
    ]);

    doctors.forEach((doctor) => {
      counts.set(doctor.specialization, (counts.get(doctor.specialization) || 0) + 1);
    });

    return Array.from(counts.entries()).map(([name, count]) => ({
      id: name,
      name,
      badge: name.slice(0, 2).toUpperCase(),
      summary: summaries.get(name) || "Specialist consultations with live appointment slots.",
      count,
    }));
  }, [doctors]);

  const filteredDoctors = useMemo(() => {
    if (!selectedSpecialistId) {
      return [];
    }

    return doctors.filter((doctor) => doctor.specialization === selectedSpecialistId);
  }, [doctors, selectedSpecialistId]);

  const selectedDoctor = doctors.find(
    (doctor) => String(doctor.doctorId) === String(selectedDoctorId)
  );

  const availableSlots = useMemo(() => {
    if (!selectedDoctor) {
      return [];
    }

    const slots = [];
    const [startHour, startMinute] = selectedDoctor.availableFrom.split(":").map(Number);
    const [endHour, endMinute] = selectedDoctor.availableTo.split(":").map(Number);
    const start = startHour * 60 + startMinute;
    const end = endHour * 60 + endMinute;

    for (let time = start; time < end; time += 45) {
      const hours = Math.floor(time / 60);
      const minutes = time % 60;
      const suffix = hours >= 12 ? "PM" : "AM";
      const hour12 = hours % 12 || 12;
      slots.push(
        `${String(hour12).padStart(2, "0")}:${String(minutes).padStart(2, "0")} ${suffix}`
      );
    }

    return slots;
  }, [selectedDoctor]);

  const selectedSpecialist = specialists.find(
    (specialist) => specialist.id === selectedSpecialistId
  );

  useEffect(() => {
    if (searchPreset?.specialty && !selectedSpecialistId) {
      const matchingSpecialist = specialists.find(
        (specialist) => specialist.id === searchPreset.specialty
      );

      if (matchingSpecialist) {
        setSelectedSpecialistId(matchingSpecialist.id);
      }
    }

    if (searchPreset?.date && !selectedDate) {
      setSelectedDate(searchPreset.date);
    }

    if (
      searchPreset?.consultation &&
      CONSULTATION_TYPES.includes(searchPreset.consultation)
    ) {
      setConsultationType(searchPreset.consultation);
    }
  }, [searchPreset, selectedDate, selectedSpecialistId, specialists]);

  const selectedDoctorPricing = useMemo(() => {
    if (!selectedDoctor) {
      return [];
    }

    const yearsOfExperience = Number.parseInt(selectedDoctor.experience, 10) || 6;
    const hospitalFee =
      320 +
      (SPECIALTY_PREMIUMS[selectedDoctor.specialization] ?? 130) +
      Math.min(yearsOfExperience * 12, 180);

    return [
      {
        type: "Hospital Visit",
        fee: hospitalFee,
        note: "Most affordable in-clinic consultation at the hospital desk.",
      },
      {
        type: "Online Review",
        fee: hospitalFee + 240,
        note: "Video or phone review with the same doctor from home.",
      },
      {
        type: "Home Visit",
        fee: hospitalFee + 620,
        note: "Doctor travel and doorstep consultation for higher convenience.",
      },
    ];
  }, [selectedDoctor]);

  const selectedConsultation = selectedDoctorPricing.find(
    (pricing) => pricing.type === consultationType
  );

  const handleSpecialistSelect = (specialistId) => {
    setSelectedSpecialistId(specialistId);
    setSelectedDoctorId("");
    setSelectedSlot("");
    setMessage("");
  };

  const handleDoctorSelect = (doctorId) => {
    setSelectedDoctorId(String(doctorId));
    setSelectedSlot("");
    setMessage("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (
      !selectedSpecialist ||
      !selectedDoctor ||
      !selectedDate ||
      !selectedSlot ||
      !patientName
    ) {
      setMessage("Please choose a specialist, doctor, patient name, date, and time slot.");
      return;
    }

    try {
      const appointment = await createAppointment({
        doctorId: selectedDoctor.doctorId,
        userName: patientName,
        userEmail: patientEmail,
        symptoms: patientConcern,
        appointmentDate: selectedDate,
        appointmentTime: selectedSlot,
        consultationType,
      });

      localStorage.setItem("smartQueueLastAppointment", JSON.stringify(appointment));
      setMessage(
        `Appointment reserved with ${appointment.doctorName}. ${appointment.consultationType} is confirmed for ₹${appointment.consultationFee}. Token ${appointment.tokenNumber} is ${appointment.status}.`
      );
      showToast(
        `Appointment booked with ${appointment.doctorName}. ${appointment.consultationType} ₹${appointment.consultationFee}.`
      );
    } catch (error) {
      setMessage(error.response?.data?.message || "Unable to create appointment.");
    }
  };

  const getInitials = (name) =>
    name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join("");

  const formatPrice = (amount) => `₹${amount.toLocaleString("en-IN")}`;

  return (
    <section className="book-page">
      <div className="book-hero">
        <div>
          <p className="book-hero__eyebrow">Patient Booking</p>
          <h1 className="book-hero__title">Choose a specialist, then select the doctor</h1>
          <p className="book-hero__copy">
            This screen now loads specialists and doctors from the backend so patient
            booking stays connected to the real hospital data.
          </p>
        </div>
      </div>

      <div className="booking-shell">
        <form className="booking-form" onSubmit={handleSubmit}>
          <section className="booking-group">
            <div className="booking-group__header">
              <p className="booking-group__eyebrow">Step 1</p>
              <h2 className="booking-group__title">All specialists list</h2>
            </div>

            <div className="specialist-grid">
              {specialists.map((specialist) => (
                <button
                  key={specialist.id}
                  type="button"
                  className={
                    selectedSpecialistId === specialist.id
                      ? "specialist-card specialist-card--active"
                      : "specialist-card"
                  }
                  onClick={() => handleSpecialistSelect(specialist.id)}
                >
                  <span className="specialist-card__badge">{specialist.badge}</span>
                  <span className="specialist-card__name">{specialist.name}</span>
                  <span className="specialist-card__summary">{specialist.summary}</span>
                  <span className="specialist-card__count">
                    {specialist.count} doctors available
                  </span>
                </button>
              ))}
            </div>
          </section>

          <section className="booking-group">
            <div className="booking-group__header">
              <p className="booking-group__eyebrow">Step 2</p>
              <h2 className="booking-group__title">Doctors under the selected specialist</h2>
            </div>

            {selectedSpecialist ? (
              <>
                <p className="doctor-section__notice">
                  Showing doctors for <strong>{selectedSpecialist.name}</strong>
                </p>

                <div className="doctor-options">
                  {filteredDoctors.map((doctor) => (
                    <button
                      key={doctor.doctorId}
                      type="button"
                      className={
                        selectedDoctorId === String(doctor.doctorId)
                          ? "doctor-option doctor-option--active"
                          : "doctor-option"
                      }
                      onClick={() => handleDoctorSelect(doctor.doctorId)}
                    >
                      <span className="doctor-option__avatar">
                        {getInitials(doctor.doctorName)}
                      </span>
                      <span className="doctor-option__content">
                        <span className="doctor-option__name">{doctor.doctorName}</span>
                        <span className="doctor-option__specialty">
                          {doctor.specialization}
                        </span>
                        <span className="doctor-option__meta">
                          {doctor.experience} • {doctor.clinic}
                        </span>
                        <span className="doctor-option__fees">
                          {buildDoctorFeeLine(doctor, formatPrice)}
                        </span>
                      </span>
                    </button>
                  ))}
                </div>
              </>
            ) : (
              <div className="doctor-empty">
                Click any specialist above and the matching doctors will appear here.
              </div>
            )}
          </section>

          <section className="booking-group">
            <div className="booking-group__header">
              <p className="booking-group__eyebrow">Step 3</p>
              <h2 className="booking-group__title">Choose visit type, date, and time slot</h2>
            </div>

            {selectedDoctor ? (
              <div className="consultation-grid">
                {selectedDoctorPricing.map((pricing) => (
                  <button
                    key={pricing.type}
                    type="button"
                    className={
                      consultationType === pricing.type
                        ? "consultation-card consultation-card--active"
                        : "consultation-card"
                    }
                    onClick={() => setConsultationType(pricing.type)}
                  >
                    <span className="consultation-card__type">{pricing.type}</span>
                    <strong className="consultation-card__price">
                      {formatPrice(pricing.fee)}
                    </strong>
                    <span className="consultation-card__note">{pricing.note}</span>
                  </button>
                ))}
              </div>
            ) : (
              <p className="booking-hint">
                Select a doctor first and the visit prices will appear here.
              </p>
            )}

            <div className="booking-fields">
              <label className="booking-field">
                <span className="booking-field__label">Patient name</span>
                <input
                  type="text"
                  value={patientName}
                  onChange={(e) => setPatientName(e.target.value)}
                />
              </label>

              <label className="booking-field">
                <span className="booking-field__label">Patient email</span>
                <input
                  type="email"
                  value={patientEmail}
                  onChange={(e) => setPatientEmail(e.target.value)}
                />
              </label>

              <label className="booking-field">
                <span className="booking-field__label">Preferred date</span>
                <input
                  type="date"
                  min={minDate}
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                />
              </label>

              <label className="booking-field booking-field--wide">
                <span className="booking-field__label">Patient concern</span>
                <textarea
                  rows="4"
                  placeholder="Describe symptoms, follow-up reason, or special instructions."
                  value={patientConcern}
                  onChange={(e) => setPatientConcern(e.target.value)}
                />
              </label>
            </div>

            <div className="slot-grid">
              {availableSlots.map((slot) => (
                <button
                  key={slot}
                  type="button"
                  className={
                    selectedSlot === slot
                      ? "slot-chip slot-chip--active"
                      : "slot-chip"
                  }
                  onClick={() => setSelectedSlot(slot)}
                >
                  {slot}
                </button>
              ))}
            </div>

            {!selectedDoctor && !loading && (
              <p className="booking-hint">
                Select a doctor first to view the available time slots.
              </p>
            )}

            {loading && (
              <p className="booking-hint">Loading specialists and doctors from backend...</p>
            )}
          </section>

          <button className="booking-submit" type="submit">
            Confirm Appointment
          </button>

          {message && <p className="booking-message">{message}</p>}
        </form>

        <aside className="booking-summary">
          <p className="booking-summary__eyebrow">Booking Summary</p>
          <h2 className="booking-summary__title">Selected appointment</h2>

            <div className="booking-summary__card">
              <div>
                <span className="booking-summary__label">Specialist needed</span>
                <strong>{selectedSpecialist?.name || "Choose a specialist"}</strong>
              </div>

              <div>
                <span className="booking-summary__label">Doctor</span>
                <strong>{selectedDoctor?.doctorName || "Select a doctor"}</strong>
              </div>

              <div>
                <span className="booking-summary__label">Visit type</span>
                <strong>{selectedConsultation?.type || consultationType}</strong>
              </div>

              <div>
                <span className="booking-summary__label">Estimated fee</span>
                <strong>
                  {selectedConsultation ? formatPrice(selectedConsultation.fee) : "Select a doctor"}
                </strong>
              </div>

              <div>
                <span className="booking-summary__label">Date</span>
                <strong>{selectedDate || "Choose a date"}</strong>
              </div>

              <div>
                <span className="booking-summary__label">Time slot</span>
                <strong>{selectedSlot || "Pick a slot"}</strong>
              </div>
            </div>

          <p className="booking-summary__note">
            {patientConcern
              ? patientConcern
              : "Add a short patient concern note so the doctor understands the visit before arrival."}
          </p>
        </aside>
      </div>
    </section>
  );
}

function getDoctorConsultationFees(doctor) {
  const yearsOfExperience = Number.parseInt(doctor.experience, 10) || 6;
  const hospitalFee =
    320 +
    (SPECIALTY_PREMIUMS[doctor.specialization] ?? 130) +
    Math.min(yearsOfExperience * 12, 180);

  return {
    hospitalVisit: hospitalFee,
    onlineReview: hospitalFee + 240,
    homeVisit: hospitalFee + 620,
  };
}

function buildDoctorFeeLine(doctor, formatPrice) {
  const pricing = getDoctorConsultationFees(doctor);
  return `Hospital ${formatPrice(pricing.hospitalVisit)} • Online ${formatPrice(
    pricing.onlineReview
  )} • Home ${formatPrice(pricing.homeVisit)}`;
}

export default BookAppointment;
