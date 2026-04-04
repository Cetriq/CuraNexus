import { useUser, roleMetadata, type UserRole } from '@/contexts/UserContext'
import { cn } from '@/lib/utils'
import { ChevronDown, Check } from 'lucide-react'
import { useState, useRef, useEffect } from 'react'

export function RoleSelector() {
  const { user, setRole, availableRoles } = useUser()
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Stäng dropdown vid klick utanför
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const currentRoleMeta = roleMetadata[user.role]

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-3 w-full p-2 rounded-lg hover:bg-sidebar-accent transition-colors"
      >
        <div className={cn(
          'h-8 w-8 rounded-full flex items-center justify-center text-white text-sm font-medium',
          currentRoleMeta.color
        )}>
          {user.initials}
        </div>
        <div className="flex-1 min-w-0 text-left">
          <p className="text-sm font-medium text-sidebar-foreground truncate">
            {user.name}
          </p>
          <p className="text-xs text-muted-foreground truncate">
            {currentRoleMeta.label}
          </p>
        </div>
        <ChevronDown className={cn(
          'h-4 w-4 text-muted-foreground transition-transform',
          isOpen && 'rotate-180'
        )} />
      </button>

      {isOpen && (
        <div className="absolute bottom-full left-0 right-0 mb-1 bg-popover border border-border rounded-lg shadow-lg overflow-hidden z-50">
          <div className="p-2 border-b border-border bg-muted/50">
            <p className="text-xs font-medium text-muted-foreground px-2">
              Byt roll (demo)
            </p>
          </div>
          <div className="max-h-64 overflow-y-auto">
            {availableRoles.map((role) => {
              const meta = roleMetadata[role]
              const isSelected = role === user.role
              return (
                <button
                  key={role}
                  onClick={() => {
                    setRole(role as UserRole)
                    setIsOpen(false)
                  }}
                  className={cn(
                    'flex items-center gap-3 w-full p-2 hover:bg-accent transition-colors',
                    isSelected && 'bg-accent'
                  )}
                >
                  <div className={cn(
                    'h-8 w-8 rounded-full flex items-center justify-center text-white text-xs font-medium',
                    meta.color
                  )}>
                    {role.slice(0, 2)}
                  </div>
                  <div className="flex-1 min-w-0 text-left">
                    <p className="text-sm font-medium truncate">
                      {meta.label}
                    </p>
                    <p className="text-xs text-muted-foreground truncate">
                      {meta.description}
                    </p>
                  </div>
                  {isSelected && (
                    <Check className="h-4 w-4 text-primary" />
                  )}
                </button>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
