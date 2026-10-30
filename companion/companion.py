
from flask import Flask, jsonify, request
import subprocess
import secrets
import time
import hmac

app = Flask(__name__)

# Temporary pairing code: valid for five minutes and one successful pairing.
PAIRING_CODE = f"{secrets.randbelow(1000000):06d}"
PAIRING_EXPIRES = time.time() + 300
PAIRING_USED = False

# Device tokens issued after successful pairing.
DEVICE_TOKENS = set()


@app.route("/status")
def status():
    return jsonify({
        "app": "SkyTab Companion",
        "status": "online"
    })


@app.route("/pair", methods=["POST"])
def pair():
    global PAIRING_USED

    data = request.get_json(silent=True) or {}
    supplied_code = str(data.get("code", ""))

    if PAIRING_USED or time.time() > PAIRING_EXPIRES:
        return jsonify({"error": "Pairing code expired. Restart the companion."}), 403

    if not hmac.compare_digest(supplied_code, PAIRING_CODE):
        return jsonify({"error": "Incorrect pairing code"}), 401

    PAIRING_USED = True
    device_token = secrets.token_urlsafe(32)
    DEVICE_TOKENS.add(device_token)

    return jsonify({
        "status": "paired",
        "token": device_token
    })


@app.route("/power/<action>", methods=["POST"])
def power(action):
    token = request.headers.get("X-SkyTab-Token", "")

    if not any(hmac.compare_digest(token, valid) for valid in DEVICE_TOKENS):
        return jsonify({"error": "Unauthorized"}), 401

    if action == "sleep":
        subprocess.Popen([
            "rundll32.exe",
            "powrprof.dll,SetSuspendState",
            "0,1,0"
        ])

    elif action == "shutdown":
        subprocess.Popen(["shutdown", "/s", "/t", "30"])

    else:
        return jsonify({"error": "Invalid action"}), 400

    return jsonify({"status": action + " requested"})


if __name__ == "__main__":
    print("\nSkyTab Companion is running.")
    print("Pairing code:", PAIRING_CODE)
    print("Pairing code expires in 5 minutes.")
    print("Keep this window open.\n")

    app.run(host="0.0.0.0", port=8765, debug=False)
