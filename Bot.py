import os

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

@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    bot.reply_to(message, "Welcome to the GummySylic bot ! + I will excellent your prompts with AI-powered enhancements. Enjoy your experience, " + message.from_user.first_name + "!")


@bot.message_handler(func=lambda message: True)
def echo_all(message):
    bot.reply_to(message, message.text)
bot.polling()