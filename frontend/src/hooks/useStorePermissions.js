import { useState, useEffect } from 'react';
import { storeService } from '../services/storeService';
import { useAuth } from '../contexts/AuthContext';

// Permission constants matching backend
export const PERMISSIONS = {
  VIEW_ONLY: 0,
  EDIT_PRODUCTS: 1,
  EDIT_POLICIES: 2,
  BID_APPROVAL: 3
};

export const useStorePermissions = (storeId) => {
  const { currentUser } = useAuth();
  const [permissions, setPermissions] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPermissions = async () => {
      if (!currentUser || !storeId) {
        setPermissions(null);
        return;
      }

      setLoading(true);
      setError(null);
      
      try {
        const userPermissions = await storeService.getCurrentUserPermissions(storeId, currentUser.userName);
        setPermissions(userPermissions);
      } catch (err) {
        console.error('Error fetching user permissions:', err);
        setError(err.message);
        // Set default "no permissions" state
        setPermissions({
          role: 'NONE',
          canEditProducts: false,
          canEditPolicies: false,
          canApproveBids: false,
          canManageUsers: false,
          permissions: []
        });
      } finally {
        setLoading(false);
      }
    };

    fetchPermissions();
  }, [currentUser, storeId]);

  // Helper functions for common permission checks
  const hasPermission = (permissionCode) => {
    return permissions?.permissions?.includes(permissionCode) || false;
  };

  const canEditProducts = permissions?.canEditProducts || false;
  const canEditPolicies = permissions?.canEditPolicies || false;
  const canApproveBids = permissions?.canApproveBids || false;
  const canManageUsers = permissions?.canManageUsers || false;
  const isOwnerOrFounder = ['OWNER', 'FOUNDER'].includes(permissions?.role);
  const isManager = permissions?.role === 'MANAGER';
  const hasAnyRole = permissions?.role !== 'NONE';

  return {
    permissions,
    loading,
    error,
    hasPermission,
    canEditProducts,
    canEditPolicies,
    canApproveBids,
    canManageUsers,
    isOwnerOrFounder,
    isManager,
    hasAnyRole,
    role: permissions?.role || 'NONE',
    refresh: () => {
      if (currentUser && storeId) {
        setLoading(true);
        storeService.getCurrentUserPermissions(storeId, currentUser.userName)
          .then(setPermissions)
          .catch(err => {
            setError(err.message);
            setPermissions({
              role: 'NONE',
              canEditProducts: false,
              canEditPolicies: false,
              canApproveBids: false,
              canManageUsers: false,
              permissions: []
            });
          })
          .finally(() => setLoading(false));
      }
    }
  };
}; 