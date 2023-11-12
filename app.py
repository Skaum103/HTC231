from flask import (
    Flask,
    request,
    session,
    send_file,
)
from werkzeug.utils import secure_filename  # Import secure_filename
from identify import identify_gpt

app = Flask(__name__, template_folder="templates", static_url_path="/static")
app.secret_key = "super secret key"


def check(username, password):
    if username == "lionel":
        return password == "antonela240687", 1
    if username == "cristiano":
        return password == "mariamadrid1985jr", 2
    if username == "neymar":
        return password == "#Q5%7Mj5N&xDP", 3
    if username == "karim":
        return password == "xiTSM3nTr6j^m", 4
    if username == "root":
        return password == "GTfCR38n3A", 5

    return False, 0

def identify(image):
    # Generate a secure filename
    filename = secure_filename(image.filename)
    # Save the image with a unique filename
    image.save(f"images/{filename}")
    path = "images/"+filename
    response = identify_gpt(path)
    result = response['choices'][0]['message']['content']
    print(result)

    return result

@app.route("/robots.txt")
def robots():
    return send_file("static/robots.txt")

@app.route("/identify", methods=["POST"])
def login():
    if "userID" in session:
        return identify(request.files['image'])

    status = 200
    result = None

    if request.method == "POST":
        try:
            username = request.form.get('username')
            password = request.form.get('password')

            result, user_id = check(username, password)

            print(result)
            result = "success" if result else "fail"

            status = 200 if result == "success" else 403

            if result == "success":
                session["userName"] = username
                session["userID"] = user_id
                return identify(request.files['image']), status

        except Exception as e:
            print(f"Error: {e}")
            result = "fail"
            status = 400

    return result, status

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True, port=3000)

