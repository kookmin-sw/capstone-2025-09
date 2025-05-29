import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { createJSONStorage } from 'zustand/middleware';

const useUserStore = create(
  persist(
    (set) => ({
      user: null,
      isLoggingOut: false,
      setUser: (user) => set({ user }),
      clearUser: () => set({ user: null }),
      setIsLoggingOut: (value) => set({ isLoggingOut: value }),
    }),
    {
      name: 'user-storage',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

export default useUserStore;
