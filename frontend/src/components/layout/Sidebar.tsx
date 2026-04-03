import { Link, useLocation } from '@tanstack/react-router'
import {
  Users,
  Stethoscope,
  ClipboardList,
  Calendar,
  Pill,
  FileText,
  FlaskConical,
  LayoutDashboard,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const navigation = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Patienter', href: '/patients', icon: Users },
  { name: 'Vårdkontakter', href: '/encounters', icon: Stethoscope },
  { name: 'Uppgifter', href: '/tasks', icon: ClipboardList },
  { name: 'Bokningar', href: '/booking', icon: Calendar },
  { name: 'Läkemedel', href: '/medication', icon: Pill },
  { name: 'Remisser', href: '/referrals', icon: FileText },
  { name: 'Labb', href: '/lab', icon: FlaskConical },
]

export function Sidebar() {
  const location = useLocation()

  return (
    <div className="flex h-full w-64 flex-col bg-sidebar border-r border-sidebar-border">
      <div className="flex h-16 items-center px-6 border-b border-sidebar-border">
        <h1 className="text-xl font-bold text-sidebar-foreground">CuraNexus</h1>
      </div>
      <nav className="flex-1 space-y-1 px-3 py-4">
        {navigation.map((item) => {
          const isActive = location.pathname === item.href ||
            (item.href !== '/' && location.pathname.startsWith(item.href))
          return (
            <Link
              key={item.name}
              to={item.href}
              className={cn(
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                  : 'text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )}
            >
              <item.icon className="h-5 w-5" />
              {item.name}
            </Link>
          )
        })}
      </nav>
      <div className="border-t border-sidebar-border p-4">
        <div className="flex items-center gap-3">
          <div className="h-8 w-8 rounded-full bg-primary flex items-center justify-center text-primary-foreground text-sm font-medium">
            AK
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-sidebar-foreground truncate">
              Anna Karlsson
            </p>
            <p className="text-xs text-muted-foreground truncate">
              Läkare
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
