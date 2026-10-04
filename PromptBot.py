import base64
import os

import requests
import telebot

from dotenv import load_dotenv

load_dotenv()

bot_token = os.getenv("BOT_TOKEN")
if not bot_token:
    raise RuntimeError(
        "BOT_TOKEN environment variable is not set. "
        "Set it in a .env file or with: $env:BOT_TOKEN = 'your-token-here'"
    )
bot = telebot.TeleBot(bot_token)

backend_url = os.getenv("BACKEND_URL", "http://localhost:8080")
backend_api_key = os.getenv("BACKEND_API_KEY", "")


def generate_veo3_prompt(telegram_user, request_text: str,
                         image_base64: str = None, image_mime: str = None,
                         prompt_type: str = "veo3-video") -> dict:
    """Call the backend to build a Veo 3 short-video prompt.
    Keyword arguments:
    telegram_user - Telegram user object (id, username, first_name)
    request_text:str - What the user wants in the video
    image_base64:str - Optional reference image encoded in base64
    image_mime:str - Mime type of the image (e.g. image/jpeg)
    prompt_type:str - veo3-video (general) or veo3-fashion (clothing sales)
    Return:dict - JSON data
    """
    url = f"{backend_url}/api/v1/prompts/generate"
    headers = {"X-API-KEY": backend_api_key} if backend_api_key else {}
    payload = {
        "telegramUserId": telegram_user.id,
        "username": telegram_user.username,
        "firstName": telegram_user.first_name,
        "request": request_text,
        "imageBase64": image_base64,
        "imageMimeType": image_mime,
        "promptType": prompt_type,
    }
    response = requests.post(url, json=payload, headers=headers, timeout=120)
    response.raise_for_status()

    return response.json()


def download_photo_base64(message) -> str:
    """Download the largest photo in the message and return it as base64."""
    file_id = message.photo[-1].file_id
    file_info = bot.get_file(file_id)
    file_bytes = bot.download_file(file_info.file_path)
    return base64.b64encode(file_bytes).decode("utf-8")


def send_long_message(chat_id, text, parse_mode=None):
    """Split messages over Telegram's 4096-char limit into chunks."""
    for start in range(0, len(text), 4000):
        bot.send_message(chat_id, text[start:start + 4000], parse_mode=parse_mode)


@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    bot.reply_to(message, "Welcome to the GummySylic prompt bot! Send /prompt for a general Veo 3 video prompt, /fashion for a clothing-sales video with an AI model, or /storyboard to turn an idea into a full script + scene-by-scene Veo 3 prompts, " + message.from_user.first_name + "!")


@bot.message_handler(commands=['prompt'])
def prompt_handler(message):
    text = "Describe the short video you want, or send a *photo* with your idea as the caption.\nExample: _a golden retriever running on a beach at sunset_"
    sent_msg = bot.send_message(message.chat.id, text, parse_mode="Markdown")
    bot.register_next_step_handler(sent_msg, request_handler, "veo3-video")


@bot.message_handler(commands=['fashion'])
def fashion_handler(message):
    text = "Send a *photo of the garment* you want to sell (caption optional: style, target customer, vibe...).\nExample caption: _ao so mi linen trang, phong cach cong so, khach nu 25-35_"
    sent_msg = bot.send_message(message.chat.id, text, parse_mode="Markdown")
    bot.register_next_step_handler(sent_msg, request_handler, "veo3-fashion")


@bot.message_handler(commands=['storyboard'])
def storyboard_handler(message):
    text = "Describe your *video idea* (topic, story, product...). I'll return a full script table (10s scenes, voiceover) plus a Veo 3 prompt per scene."
    sent_msg = bot.send_message(message.chat.id, text, parse_mode="Markdown")
    bot.register_next_step_handler(sent_msg, request_handler, "veo3-storyboard")


def request_handler(message, prompt_type="veo3-video"):
    image_base64 = None
    image_mime = None
    if message.content_type == 'photo':
        default_caption = (
            "Build a vertical fashion sales video prompt: one AI model wearing exactly this garment."
            if prompt_type == "veo3-fashion"
            else "Build a realistic short video prompt from this image."
        )
        request_text = message.caption or default_caption
        image_base64 = download_photo_base64(message)
        image_mime = "image/jpeg"
    elif message.content_type == 'text':
        request_text = message.text
    else:
        bot.send_message(message.chat.id, "Please send text or a photo. Try /prompt again.")
        return

    bot.send_message(message.chat.id, "Building your Veo 3 prompt, please wait...")
    try:
        result = generate_veo3_prompt(message.from_user, request_text, image_base64, image_mime, prompt_type)
        data = result.get("data") or {}
        generated = data.get("generatedPrompt")
        if not generated or generated == "null":
            raise ValueError(f"backend returned empty prompt: {result}")
        send_long_message(message.chat.id, f"*Your Veo 3 prompt:*\n\n{generated}", parse_mode="Markdown")
    except Exception as e:
        bot.send_message(
            message.chat.id,
            "Sorry, I couldn't build the prompt right now. "
            "Please try /prompt again in a moment."
        )
        print(f"Error generating prompt: {e}")


bot.infinity_polling()
