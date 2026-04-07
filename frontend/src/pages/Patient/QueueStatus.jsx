import { useContext, useEffect, useMemo, useState } from "react";
import { AuthContext } from "../../context/auth-context";
import { getQueue } from "../../services/api";

function QueueStatus() {
  const { user } = useContext(AuthContext);
  const [queue, setQueue] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let ignore = false;

    const fetchQueue = async () => {
      try {
        const data = await getQueue();
        if (!ignore) {
          setQueue(data);
        }
      } catch (error) {
        if (!ignore) {
          setMessage(error.response?.data?.message || "Unable to load queue status.");
        }
      }
    };

    fetchQueue();
    const interval = setInterval(fetchQueue, 10000);
    return () => {
      ignore = true;
      clearInterval(interval);
    };
  }, []);

  const lastAppointment = useMemo(() => {
    try {
      const stored = localStorage.getItem("smartQueueLastAppointment");
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }, []);

  const position = useMemo(() => {
    const index = queue.findIndex((entry) => {
      if (lastAppointment?.id) {
        return entry.id === lastAppointment.id;
      }

      if (user?.name) {
        return entry.name === user.name;
      }

      return false;
    });

    return index >= 0 ? index + 1 : null;
  }, [lastAppointment, queue, user]);

  return (
    <div className="max-w-2xl mx-auto mt-10 p-6 bg-white/70 backdrop-blur-md rounded-xl shadow-lg">
      <h1 className="text-2xl font-bold mb-6 text-gray-800">Queue Status</h1>

      {position ? (
        <div className="mb-6 text-center">
          <p className="text-lg">Your current position in queue:</p>
          <p className="text-3xl font-bold text-blue-600">{position}</p>
        </div>
      ) : (
        <p className="text-center text-gray-700 mb-6">
          No matching appointment was found for the current patient yet.
        </p>
      )}

      {message && <p className="text-center text-gray-700 mb-6">{message}</p>}

      <h2 className="text-xl font-semibold mb-4">Full Queue</h2>
      <ul className="space-y-2">
        {queue.map((patient, index) => (
          <li
            key={patient.id}
            className={`p-3 rounded shadow ${
              index === 0 ? "bg-green-100 font-bold" : "bg-white/70 backdrop-blur-md"
            }`}
          >
            {index + 1}. {patient.name} - {patient.doctorName} ({patient.status})
          </li>
        ))}
      </ul>
    </div>
  );
}

export default QueueStatus;
