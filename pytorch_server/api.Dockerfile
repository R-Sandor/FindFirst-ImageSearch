# Used https://stackoverflow.com/questions/48561981/activate-python-virtualenv-in-dockerfile as reference
FROM python:3.9.18-slim as build
ENV PYTHONUNBUFFERED 1

WORKDIR /app/

RUN python -m venv /opt/venv
# Enable venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./requirements.txt /app/requirements.txt
RUN pip install -Ur requirements.txt

FROM python:3.9.18-slim as runner
WORKDIR /app/
COPY --from=build /opt/venv /opt/venv
# Enable venv
ENV PATH="/opt/venv/bin:$PATH"
COPY . /app
EXPOSE 5000
ENV FLASK_ENV="development"
ENV FLASK_APP=server.py
CMD ["flask", "run", "--host=0.0.0.0", "--port=5000"]