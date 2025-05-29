import { Navigate, Outlet } from 'react-router-dom';
import useUserStore from '../../utils/userStore';

const ProtectedRoute = () => {
  const user = useUserStore((state) => state.user);
  const isLoggingOut = useUserStore((state) => state.isLoggingOut);

  if (isLoggingOut) return null; // 🔐 로그아웃 중일 땐 아무 것도 렌더하지 않음

  return user ? <Outlet /> : <Navigate to="/sign-in" replace />;
};

export default ProtectedRoute;
