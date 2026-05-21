'use client';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/auth/store';
import { apiClient } from '@/lib/api/client';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils';
import {
  LayoutDashboard,
  Building2,
  Trophy,
  Calendar,
  LogOut,
  Layers,
  BookOpen,
  ShieldCheck,
  Swords,
} from 'lucide-react';

interface NavItem {
  href: string;
  label: string;
  icon: React.ElementType;
  exact?: boolean;
}

const playerNav: NavItem[] = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, exact: true },
  { href: '/centers', label: 'Centros', icon: Building2 },
  { href: '/tournaments', label: 'Torneos', icon: Trophy },
  { href: '/bookings', label: 'Mis reservas', icon: Calendar },
  { href: '/matches', label: 'Mis partidos', icon: Swords },
];

const adminNav: NavItem[] = [
  { href: '/admin', label: 'Panel admin', icon: ShieldCheck, exact: true },
  { href: '/admin/centers', label: 'Centros', icon: Building2 },
  { href: '/admin/fields', label: 'Pistas', icon: Layers },
  { href: '/admin/bookings', label: 'Reservas', icon: BookOpen },
  { href: '/admin/tournaments', label: 'Torneos', icon: Trophy },
];

function NavLink({ item, onClick }: { item: NavItem; onClick?: () => void }) {
  const pathname = usePathname();
  const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
  const Icon = item.icon;

  return (
    <Link
      href={item.href}
      onClick={onClick}
      className={cn(
        'flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors',
        active
          ? 'bg-slate-100 text-slate-900 font-medium'
          : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'
      )}
    >
      <Icon className="h-4 w-4 shrink-0" />
      {item.label}
    </Link>
  );
}

interface SidebarContentProps {
  onNavClick?: () => void;
}

export function SidebarContent({ onNavClick }: SidebarContentProps) {
  const router = useRouter();
  const { user, isAdmin, clearAuth } = useAuthStore();

  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '';

  const handleLogout = async () => {
    await apiClient.post('/api/v1/auth/logout').catch(() => {});
    clearAuth();
    router.replace('/login');
  };

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-14 items-center border-b px-4">
        <Link href="/dashboard" onClick={onNavClick} className="font-semibold text-slate-900 text-base">
          PadelCenter
        </Link>
      </div>

      <ScrollArea className="flex-1 px-3 py-4">
        <div className="space-y-1">
          {playerNav.map((item) => (
            <NavLink key={item.href} item={item} onClick={onNavClick} />
          ))}
        </div>

        {isAdmin() && (
          <>
            <Separator className="my-4" />
            <p className="mb-2 px-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Administración
            </p>
            <div className="space-y-1">
              {adminNav.map((item) => (
                <NavLink key={item.href} item={item} onClick={onNavClick} />
              ))}
            </div>
          </>
        )}
      </ScrollArea>

      <div className="border-t p-3">
        <div className="flex items-center gap-2">
          <Link
            href="/profile"
            onClick={onNavClick}
            className="flex flex-1 min-w-0 items-center gap-3 rounded-md px-2 py-2 hover:bg-slate-50 transition-colors"
          >
            <Avatar className="h-8 w-8 shrink-0">
              <AvatarFallback className="text-xs bg-slate-100">{initials}</AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-slate-900 truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-slate-500 truncate">{user?.email}</p>
            </div>
          </Link>
          <button
            onClick={handleLogout}
            className="shrink-0 p-2 text-slate-400 hover:text-red-500 transition-colors rounded-md hover:bg-slate-50"
            title="Cerrar sesión"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
