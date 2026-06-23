import { LucideIcon } from 'lucide-react';

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description: string;
  action?: React.ReactNode;
}

export default function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="text-center py-12">
      <Icon className="mx-auto h-10 w-10 text-slate-300 mb-3" />
      <h3 className="text-sm font-medium text-slate-900">{title}</h3>
      <p className="text-sm text-slate-500 mt-1">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
