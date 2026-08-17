import {useToast} from "../../context/ToastContext.tsx";
import {useCallback, useEffect, useState} from "react";
import type AssignorResponse from "../../services/assignor/dto/AssignorResponse.ts";
import {assignorService} from "../../services/assignor/assignor.service.ts";

export function useAssignorOptions() {
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [assignors, setAssignors] = useState<AssignorResponse[]>([]);

    const findAll = useCallback(() => {
        setLoading(true);
        assignorService.findAll()
            .then(setAssignors)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoading(false));
    }, [toast]);

    useEffect(() => {
        findAll();
    }, [findAll]);

    return {
        assignors,
        loading,
        findAll,
    };
}