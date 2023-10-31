# Used https://stackoverflow.com/questions/48561981/activate-python-virtualenv-in-dockerfile as reference
FROM python:3.9.18 as build
ENV PYTHONUNBUFFERED 1


WORKDIR /app/

RUN python -m venv /opt/venv
# Enable venv
ENV PATH="/opt/venv/bin:$PATH"
RUN pip install --upgrade pip
RUN pip install setuptools>=40.8.0
RUN pip install asgiref>=3.2
RUN pip install flask==2.0.2
RUN pip install flask-cors
RUN pip install flask-wtf==1.0.1
RUN pip install WerkZeug==2.2.2
RUN pip install ftfy
RUN pip install regex
RUN pip install python-dotenv~=0.21.1
RUN pip install exif==1.5.0

COPY ./requirements.txt /app/requirements.txt
RUN pip install -r requirements.txt

FROM python:3.9.18 as runner
WORKDIR /app/
COPY --from=build /opt/venv /opt/venv
# Enable venv
ENV PATH="/opt/venv/bin:$PATH"
COPY . /app
EXPOSE 5000
ENV FLASK_ENV="development"
ENV FLASK_APP=server.py
CMD ["flask", "run", "--host=0.0.0.0", "--port=5000"]
