# 构建阶段
FROM node:18-alpine AS build-stage

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

# 生产阶段
FROM nginx:alpine

# 复制构建产物到nginx默认静态目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制自定义nginx配置（如果需要）
# COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

