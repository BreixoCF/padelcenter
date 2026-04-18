'use client';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/auth/store';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { User, LogOut, Settings } from 'lucide-react';
import { apiClient } from '@/lib/api/client';

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, clearAuth, isAdmin } = useAuthStore();

  const handleLogout = async () => {
    await apiClient.post('/api/v1/auth/logout').catch(() => {});
    clearAuth();
    router.replace('/login');
  };

  const initials = user
    ? `${user.firstName[0] ?? ''}${user.lastName[0] ?? ''}`.toUpperCase()
    : '';

  const navLink = (href: string, label: string) => (
    <Link
      href={href}
      className={
        pathname.startsWith(href)
          ? 'text-slate-900 font-medium'
          : 'text-slate-500 hover:text-slate-900'
      }
    >
      {label}
    </Link>
  );

  return (
    <header className="border-b bg-white sticky top-0 z-50">
      <div className="container mx-auto px-4 max-w-6xl">
        <div className="h-14 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link href="/" className="font-semibold text-slate-900">
              PadelCenter
            </Link>
            <nav className="hidden md:flex items-center gap-4 text-sm">
              {navLink('/centers', 'Centros')}
              {navLink('/tournaments', 'Torneos')}
              {isAuthenticated && isAdmin() && navLink('/admin', 'Admin')}
            </nav>
          </div>

          <div>
            {isAuthenticated ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <button className="outline-none">
                    <Avatar className="h-8 w-8 cursor-pointer">
                      <AvatarFallback className="text-xs bg-slate-100">
                        {initials}
                      </AvatarFallback>
                    </Avatar>
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <div className="px-2 py-1.5 text-sm">
                    <p className="font-medium">{user?.firstName}</p>
                    <p className="text-slate-500 text-xs truncate">{user?.email}</p>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link href="/dashboard">
                      <User className="h-4 w-4 mr-2" />
                      Mis reservas
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link href="/profile">
                      <Settings className="h-4 w-4 mr-2" />
                      Perfil
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout} className="text-red-600">
                    <LogOut className="h-4 w-4 mr-2" />
                    Cerrar sesión
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <Link href="/login">
                <Button size="sm">Iniciar sesión</Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
