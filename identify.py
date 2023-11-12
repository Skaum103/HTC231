from openai import OpenAI
import base64
import requests

def identify_gpt(path):
    # OpenAI API Key
    api_key="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"
    # Function to encode the image
    def encode_image(image_path):
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode('utf-8')

    # Path to your image
    image_path = path

    # Getting the base64 string
    base64_image = encode_image(image_path)

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    payload = {
        "model": "gpt-4-vision-preview",
        "messages": [
          {
            "role": "user",
            "content": [
              {
                "type": "text",
                "text": "Choose what kind of garbage it is.  Select from a list. [Recyclables, Organics, Garbage]. Give a single word answer."
              },
              {
                "type": "image_url",
                "image_url": {
                  "url": f"data:image/jpeg;base64,{base64_image}"
                }
              }
            ]
          }
        ],
        "max_tokens": 300
    }
    response = requests.post("https://api.openai.com/v1/chat/completions", headers=headers, json=payload)
    print(response.json())
    return response


if __name__ == "__main__":
    identify_gpt("images/test.png")