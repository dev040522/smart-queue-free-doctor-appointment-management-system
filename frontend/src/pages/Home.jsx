import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import hospitalIllustration from "../assets/hospital-illustration.svg";
import "./Home.css";

function Home() {
  const navigate = useNavigate();
  const metrics = [
    { value: "06 min", label: "average lobby stay" },
    { value: "18", label: "specialist desks live today" },
    { value: "94%", label: "patients called on schedule" },
  ];

  const searchHighlights = [
    { value: "1,200+", label: "Verified Doctors" },
    { value: "40+", label: "Specialties" },
    { value: "50k+", label: "Happy Patients" },
  ];

  const searchSpecialties = [
    { value: "Pediatrics", label: "Pediatrician" },
    { value: "Cardiology", label: "Cardiologist" },
    { value: "Orthopedics", label: "Orthopedic" },
    { value: "Neurology", label: "Neurologist" },
    { value: "General Medicine", label: "General Physician" },
  ];

  const cityOptions = ["Hyderabad", "Mumbai", "Bengaluru", "Delhi", "Chennai"];
  const consultationOptions = ["Home Visit", "Hospital Visit", "Online Review"];
  const defaultSearchDate = new Date().toISOString().split("T")[0];
  const [searchForm, setSearchForm] = useState({
    specialty: "Pediatrics",
    city: "Hyderabad",
    date: defaultSearchDate,
    consultation: "Home Visit",
  });

  const queueRows = [
    { wing: "Cardiology", token: "A-12", status: "Now calling", wait: "08 min" },
    { wing: "Pediatrics", token: "C-03", status: "Fast track", wait: "05 min" },
    { wing: "Orthopedics", token: "B-09", status: "Queue moving", wait: "11 min" },
    { wing: "Diagnostics", token: "D-14", status: "Ready for scan", wait: "04 min" },
  ];

  const hospitalWings = [
    {
      code: "ER",
      title: "Emergency Ready",
      detail: "Ambulance handoff, triage, and rapid routing connected to specialist teams.",
    },
    {
      code: "DX",
      title: "Diagnostics Grid",
      detail: "Lab and scan timings stay aligned with consultations so the floor moves faster.",
    },
    {
      code: "OP",
      title: "Specialist OPD",
      detail: "Separate arrival windows reduce crowding and keep appointment blocks clean.",
    },
    {
      code: "IC",
      title: "Critical Care",
      detail: "ICU support and high-priority escalation remain visible to hospital staff instantly.",
    },
  ];

  const doctors = [
    {
      name: "Dr. Aisha Kapoor",
      specialty: "Cardiology",
      timing: "09:00 AM - 01:00 PM",
      note: "Consults for chest pain review, ECG follow-up, and heart risk screening.",
    },
    {
      name: "Dr. Rohan Mehta",
      specialty: "Orthopedics",
      timing: "10:30 AM - 03:30 PM",
      note: "Supports fracture care, sports injuries, and joint mobility recovery plans.",
    },
    {
      name: "Dr. Sana Joseph",
      specialty: "Pediatrics",
      timing: "11:00 AM - 05:00 PM",
      note: "Child-friendly consultations for fever, growth review, and vaccination follow-up.",
    },
    {
      name: "Dr. Vikram Rao",
      specialty: "Neurology",
      timing: "02:00 PM - 07:00 PM",
      note: "Treats migraines, dizziness, nerve conditions, and long-term neurological care.",
    },
  ];

  const patientJourney = [
    {
      step: "01",
      title: "Choose a specialist",
      detail: "Patients begin with the department they need, not a confusing general queue.",
    },
    {
      step: "02",
      title: "Lock the doctor and slot",
      detail: "Appointment timing stays visible so the patient arrives for a precise window.",
    },
    {
      step: "03",
      title: "Follow the live board",
      detail: "The queue updates in real time while the hospital floor stays calmer and lighter.",
    },
  ];

  const liveTape = [
    "Queue board synced with OPD desks",
    "Cardiology lane is calling token A-12",
    "Diagnostics window opened for scan arrivals",
    "Pediatrics fast-track line updated in real time",
  ];

  const getInitials = (name) =>
    name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join("");

  const handleSearchChange = (field) => (event) => {
    setSearchForm((current) => ({
      ...current,
      [field]: event.target.value,
    }));
  };

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    navigate("/patient/book", {
      state: {
        searchPreset: {
          specialty: searchForm.specialty,
          date: searchForm.date,
          city: searchForm.city,
          consultation: searchForm.consultation,
        },
      },
    });
  };

  return (
    <section className="home-stage">
      <div className="home-stage__orb home-stage__orb--left"></div>
      <div className="home-stage__orb home-stage__orb--right"></div>

      <section className="hero-grid">
        <div className="hero-command">
          <p className="hero-kicker">Sunrise Multispeciality Hospital</p>
          <div className="hero-ribbon">
            <span className="hero-ribbon__pulse"></span>
            Smart Queue control room is running live across OPD, diagnostics, and triage.
          </div>

          <h1 className="hero-heading">
            Hospital visits that feel scheduled, not chaotic.
          </h1>

          <p className="hero-copy">
            The main screen now behaves like a hospital command center: doctor slots,
            arrival rhythm, and live queue movement all visible before a patient even
            reaches the front desk.
          </p>

          <div className="hero-actions">
            <Link className="hero-button hero-button--primary" to="/register">
              Start Booking
            </Link>
            <Link className="hero-button hero-button--ghost" to="/patient/book">
              Browse Specialists
            </Link>
            <Link className="hero-button hero-button--ghost" to="/login">
              Patient Login
            </Link>
          </div>

          <div className="metric-rack">
            {metrics.map((metric, index) => (
              <article
                className="metric-card"
                key={metric.label}
                style={{ "--delay": `${index * 120}ms` }}
              >
                <span className="metric-card__value">{metric.value}</span>
                <span className="metric-card__label">{metric.label}</span>
              </article>
            ))}
          </div>
        </div>

        <aside className="hero-desk">
          <article className="desk-card">
            <div className="desk-card__header">
              <div>
                <p className="section-tag">Live Queue Board</p>
                <h2 className="desk-card__title">Current hospital rhythm</h2>
              </div>
              <span className="live-pill">Synced</span>
            </div>

            <div className="desk-card__rows">
              {queueRows.map((row, index) => (
                <div
                  className="desk-row"
                  key={row.wing}
                  style={{ "--delay": `${index * 110}ms` }}
                >
                  <div className="desk-row__copy">
                    <strong>{row.wing}</strong>
                    <span>{row.status}</span>
                  </div>
                  <div className="desk-row__meta">
                    <span className="desk-row__token">{row.token}</span>
                    <span className="desk-row__wait">{row.wait}</span>
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="arrival-card">
            <p className="section-tag">Smooth Arrival</p>
            <h2 className="arrival-card__title">Patients arrive closer to consultation time.</h2>
            <p className="arrival-card__copy">
              Smart Queue reduces lobby crowding by giving every patient a clearer window
              to enter, check in, and move directly toward the right doctor.
            </p>
            <div className="arrival-card__chips">
              <span>Token alerts</span>
              <span>Slot reminders</span>
              <span>Live movement</span>
            </div>
          </article>
        </aside>
      </section>

      <section className="care-search">
        <div className="care-search__stats">
          {searchHighlights.map((item) => (
            <article className="care-search__stat" key={item.label}>
              <strong>{item.value}</strong>
              <span>{item.label}</span>
            </article>
          ))}
        </div>

        <form className="care-search__panel" onSubmit={handleSearchSubmit}>
          <label className="care-search__field">
            <span className="care-search__label">Specialty</span>
            <select
              className="care-search__control"
              value={searchForm.specialty}
              onChange={handleSearchChange("specialty")}
            >
              {searchSpecialties.map((specialty) => (
                <option key={specialty.value} value={specialty.value}>
                  {specialty.label}
                </option>
              ))}
            </select>
          </label>

          <label className="care-search__field">
            <span className="care-search__label">City</span>
            <select
              className="care-search__control"
              value={searchForm.city}
              onChange={handleSearchChange("city")}
            >
              {cityOptions.map((city) => (
                <option key={city} value={city}>
                  {city}
                </option>
              ))}
            </select>
          </label>

          <label className="care-search__field">
            <span className="care-search__label">Date</span>
            <input
              className="care-search__control"
              type="date"
              min={defaultSearchDate}
              value={searchForm.date}
              onChange={handleSearchChange("date")}
            />
          </label>

          <label className="care-search__field">
            <span className="care-search__label">Consultation</span>
            <select
              className="care-search__control"
              value={searchForm.consultation}
              onChange={handleSearchChange("consultation")}
            >
              {consultationOptions.map((consultation) => (
                <option key={consultation} value={consultation}>
                  {consultation}
                </option>
              ))}
            </select>
          </label>

          <button className="care-search__button" type="submit">
            Search Doctors
          </button>
        </form>
      </section>

      <section className="live-tape" aria-label="Live hospital updates">
        <div className="live-tape__track">
          {[...liveTape, ...liveTape].map((item, index) => (
            <span className="live-tape__item" key={`${item}-${index}`}>
              {item}
            </span>
          ))}
        </div>
      </section>

      <section className="panorama-grid">
        <article className="panorama-card">
          <div className="panorama-card__frame">
            <img
              className="panorama-card__image"
              src={hospitalIllustration}
              alt="Modern hospital building with structured arrival lanes"
            />
          </div>

          <div className="panorama-ticket">
            <span className="panorama-ticket__label">Now boarding</span>
            <strong>A-12</strong>
            <span>Cardiology arrival lane</span>
          </div>

          <div className="panorama-note">
            <strong>Sunrise Multispeciality Hospital</strong>
            <span>Emergency care, diagnostics, ICU, pediatrics, and specialist OPD</span>
          </div>
        </article>

        <article className="wings-panel">
          <p className="section-tag">Hospital Wings</p>
          <h2 className="panel-heading">A more organized floor from entry to consultation.</h2>

          <div className="wing-grid">
            {hospitalWings.map((wing, index) => (
              <article
                className="wing-card"
                key={wing.title}
                style={{ "--delay": `${index * 110}ms` }}
              >
                <span className="wing-card__code">{wing.code}</span>
                <h3 className="wing-card__title">{wing.title}</h3>
                <p className="wing-card__detail">{wing.detail}</p>
              </article>
            ))}
          </div>
        </article>
      </section>

      <section className="specialist-floor">
        <div className="specialist-floor__header">
          <div>
            <p className="section-tag">Today's Specialist Floor</p>
            <h2 className="panel-heading">Doctors visible before the patient walks in</h2>
          </div>
          <p className="specialist-floor__copy">
            The main screen highlights real doctor names, active departments, and the
            exact timing windows patients care about most.
          </p>
        </div>

        <div className="specialist-river">
          {doctors.map((doctor, index) => (
            <article
              className="doctor-spotlight"
              key={doctor.name}
              style={{ "--delay": `${index * 120}ms` }}
            >
              <div className="doctor-spotlight__avatar">{getInitials(doctor.name)}</div>
              <span className="doctor-spotlight__specialty">{doctor.specialty}</span>
              <h3 className="doctor-spotlight__name">{doctor.name}</h3>
              <p className="doctor-spotlight__timing">{doctor.timing}</p>
              <p className="doctor-spotlight__note">{doctor.note}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="journey-panel">
        <div className="journey-panel__copy">
          <p className="section-tag">Patient Journey</p>
          <h2 className="panel-heading">A calmer path from booking to consultation.</h2>
          <p className="journey-panel__text">
            Instead of one crowded waiting room, the patient follows a sequence:
            select the right specialist, reserve a doctor, and arrive when the queue is
            actually ready.
          </p>
        </div>

        <div className="journey-rail">
          {patientJourney.map((item, index) => (
            <article
              className="journey-stop"
              key={item.step}
              style={{ "--delay": `${index * 120}ms` }}
            >
              <span className="journey-stop__step">{item.step}</span>
              <h3 className="journey-stop__title">{item.title}</h3>
              <p className="journey-stop__detail">{item.detail}</p>
            </article>
          ))}
        </div>
      </section>
    </section>
  );
}

export default Home;
