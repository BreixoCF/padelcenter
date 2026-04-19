'use client';
import { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';

interface ReportResultDialogProps {
  matchId: string;
  pairAId: string;
  pairBId: string;
  onClose: () => void;
  onSubmit: (data: { winnerPairId: string; scoreA: number; scoreB: number }) => void;
  isPending: boolean;
}

export default function ReportResultDialog({
  matchId, pairAId, pairBId, onClose, onSubmit, isPending,
}: ReportResultDialogProps) {
  const [form, setForm] = useState({
    winnerPairId: pairAId,
    scoreA: 0,
    scoreB: 0,
  });

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Reportar resultado</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>Ganador</Label>
            <Select value={form.winnerPairId} onValueChange={v => setForm(p => ({ ...p, winnerPairId: v }))}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={pairAId}>Pareja A</SelectItem>
                <SelectItem value={pairBId}>Pareja B</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label>Sets pareja A</Label>
              <Input type="number" min={0} max={3}
                value={form.scoreA}
                onChange={e => setForm(p => ({ ...p, scoreA: parseInt(e.target.value) || 0 }))} />
            </div>
            <div className="space-y-2">
              <Label>Sets pareja B</Label>
              <Input type="number" min={0} max={3}
                value={form.scoreB}
                onChange={e => setForm(p => ({ ...p, scoreB: parseInt(e.target.value) || 0 }))} />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Cancelar</Button>
          <Button onClick={() => onSubmit(form)} disabled={isPending}>
            {isPending ? 'Enviando...' : 'Confirmar resultado'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
