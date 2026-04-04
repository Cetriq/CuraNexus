import { Link, useLocation } from '@tanstack/react-router'
import { cn } from '@/lib/utils'
import { useUser } from '@/contexts/UserContext'
import { getNavigationForRole } from '@/config/navigation'
import { RoleSelector } from '@/components/RoleSelector'

export function Sidebar() {
  const location = useLocation()
  const { user } = useUser()

  // Hämta navigation baserat på användarens roll
  const navigation = getNavigationForRole(user.role)

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
      <div className="border-t border-sidebar-border p-3">
        <RoleSelector />
      </div>
    </div>
  )
}
