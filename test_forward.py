import smtplib


def load_message():
    data = None

    with open('seamless.txt', 'r') as myfile:
        data = myfile.read()

    return data


def main():
    smtp = smtplib.SMTP('localhost', 2500)
    smtp.sendmail('slee@heymrtough.com', 'seamless', load_message())


if __name__ == '__main__':
    main()
