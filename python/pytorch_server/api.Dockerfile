# Used https://stackoverflow.com/questions/48561981/activate-python-virtualenv-in-dockerfile as reference
FROM python:3.9.18 as build
ENV PYTHONUNBUFFERED 1


WORKDIR /app/

RUN python -m venv /opt/venv
# Enable venv
ENV PATH="/opt/venv/bin:$PATH"
run pip install asgiref>=3.2
run pip install flask==2.0.2
run pip install flask-cors
run pip install flask-wtf==1.0.1
run pip install WerkZeug==2.2.2
run pip install ftfy
run pip install regex
run pip install python-dotenv~=0.21.1
run pip install exif==1.5.0

COPY ./requirements.txt /app/requirements.txt
RUN pip install -Ur requirements.txt

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
