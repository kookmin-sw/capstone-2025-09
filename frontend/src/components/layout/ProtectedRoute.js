import { Navigate, Outlet } from 'react-router-dom';
import useUserStore from '../../utils/userStore';

const ProtectedRoute = () => {
  const user = useUserStore((state) => state.user); // 로그인 여부 판단

  return user ? <Outlet /> : <Navigate to="/sign-in" replace />;
};

export default ProtectedRoute;
