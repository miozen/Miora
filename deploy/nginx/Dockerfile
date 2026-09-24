FROM nginx:1.27-alpine

ARG VCS_REF=unknown
LABEL org.opencontainers.image.source="https://github.com/miozen/Miora" \
      org.opencontainers.image.revision="$VCS_REF" \
      org.opencontainers.image.title="miora-proxy"

COPY default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
