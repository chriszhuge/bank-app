import axios from 'axios';
import { ElMessage } from 'element-plus';
import 'element-plus/es/components/message/style/css'; 

const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
});



export default instance;
