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




def get_daily_horoscope(sign: str, day: str) -> dict:
    """Get daily horoscope for a zodiac sign.
    Keyword arguments:
    sign:str - Zodiac sign
    day:str - Date in format (YYYY-MM-DD) OR TODAY OR TOMORROW OR YESTERDAY
    Return:dict - JSON data
    """
    url = os.getenv("HOROSCOPE_API_URL")
    params = {"sign": sign, "day": day}
    response = requests.get(url, params)

    return response.json()


@bot.message_handler(commands=['Horoscope'])
def sign_handler(message):
    text = "What is your zodiac sign?\nChoose one: *Aries*, *Taurus*, *Gemini*, *Cancer,* *Leo*, *Virgo*, *Libra*, *Scorpio*, *Sagittarius*, *Capricorn*, *Aquarius*, and *Pisces*."
    sent_msg = bot.send_message(message.chat.id, text, parse_mode="Markdown")
    bot.register_next_step_handler(sent_msg, day_handler)
    

def day_handler(message):
    sign = message.text
    text = "For which day do you want the horoscope?\nChoose one: *TODAY*, *TOMORROW*, or *YESTERDAY*."
    sent_msg = bot.send_message(message.chat.id, text, parse_mode="Markdown")
    bot.register_next_step_handler(
        sent_msg, fetch_horoscope, sign.capitalize())
    
def fetch_horoscope(message, sign):
    day = message.text.upper()
    try:
        horoscope = get_daily_horoscope(sign, day)
        data = horoscope["data"]
        horoscope_message = f'*Horoscope:* {data["horoscope"]}\n*Sign:* {sign}\n*Day:* {data["date"]}'
        bot.send_message(message.chat.id, "Here's your horoscope!")
        bot.send_message(message.chat.id, horoscope_message, parse_mode="Markdown")
    except Exception as e:
        bot.send_message(
            message.chat.id,
            f"Sorry, I couldn't get the horoscope for *{sign}* ({day}). "
            "Please check the sign/day spelling and try /Horoscope again.",
            parse_mode="Markdown"
        )
        print(f"Error fetching horoscope: {e}")


bot.infinity_polling()