import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../context/auth-context";
import { ToastContext } from "../../context/toast-context";
import { getApiOrigin, registerUser } from "../../services/api";

function Register() {
  const navigate = useNavigate();
  const { setUser } = useContext(AuthContext);
  const { showToast } = useContext(ToastContext);
  const roleOptions = [
    {
      value: "doctor",
      title: "Doctor",
      description: "Create an account to manage appointments and queue flow.",
    },
    {
      value: "patient",
      title: "Patient",
      description: "Create an account to book appointments and track your queue.",
    },
  ];

  const [role, setRole] = useState("doctor");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const response = await registerUser({
        role,
        name,
        email,
        phoneNumber,
        password,
      });

      setUser(response.user);
      setMessage(response.message);
      showToast(
        `Registration successful. ${role === "doctor" ? "Doctor" : "Patient"} account created.`
      );
      navigate(role === "doctor" ? "/doctor/dashboard" : "/patient/dashboard");
    } catch (error) {
      if (!error.response) {
        setMessage(`Backend server is not running on ${getApiOrigin()}. Start it and try again.`);
      } else {
        setMessage(error.response?.data?.message || "Registration failed.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 shadow-lg rounded-lg">
      <h2 className="text-xl font-bold mb-4">Register</h2>
      <form onSubmit={handleRegister} className="space-y-4">
        <div>
          <label className="block mb-1 font-medium text-gray-700">
            Register as
          </label>
          <div className="grid md:grid-cols-2 gap-2">
            {roleOptions.map((option) => {
              const isSelected = role === option.value;

              return (
                <button
                  key={option.value}
                  type="button"
                  className={
                    isSelected
                      ? "w-full p-4 rounded-lg bg-blue-600 text-white"
                      : "w-full p-4 rounded-lg bg-white/70 text-gray-700 border"
                  }
                  style={isSelected ? { border: "1px solid #2563eb" } : undefined}
                  onClick={() => setRole(option.value)}
                >
                  <span className="block font-semibold mb-1">{option.title}</span>
                  <span className="block">{option.description}</span>
                </button>
              );
            })}
          </div>
        </div>

        <input
          type="text"
          placeholder="Full Name"
          className="w-full p-2 border rounded"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 border rounded"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="tel"
          placeholder="Phone Number"
          className="w-full p-2 border rounded"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 border rounded"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />
        <button className="w-full bg-blue-600 text-white py-2 rounded" disabled={loading}>
          {loading
            ? "Creating account..."
            : `Register as ${role === "doctor" ? "Doctor" : "Patient"}`}
        </button>
      </form>

      {message && <p className="mt-4 text-center text-gray-700">{message}</p>}
    </div>
  );
}

export default Register;
