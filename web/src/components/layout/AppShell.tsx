'use client';
import { useState, useEffect } from 'react';
import { Menu } from 'lucide-react';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { SidebarContent } from './Sidebar';

export default function AppShell({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  if (!mounted) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <main className="flex-1 overflow-y-auto bg-slate-50">{children}</main>
      </div>
    );
  }

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex w-60 shrink-0 flex-col border-r bg-white">
        <SidebarContent />
      </aside>

      {/* Mobile: top bar + drawer */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex h-14 items-center border-b bg-white px-4 md:hidden">
          <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="-ml-2">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-60 p-0">
              <SidebarContent onNavClick={() => setOpen(false)} />
            </SheetContent>
          </Sheet>
          <span className="ml-3 font-semibold text-slate-900">PadelCenter</span>
        </header>

        <main className="flex-1 overflow-y-auto bg-slate-50">
          {children}
        </main>
      </div>
    </div>
  );
}
