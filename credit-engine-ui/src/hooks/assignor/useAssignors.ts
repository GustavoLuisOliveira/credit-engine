import { useCallback, useState } from 'react';
import {useToast} from "../../context/ToastContext.tsx";
import type AssignorResponse from "../../services/assignor/dto/AssignorResponse.ts";
import {assignorService} from "../../services/assignor/assignor.service.ts";
import type AssignorRequest from "../../services/assignor/dto/AssignorRequest.ts";
import {validateAssignorRequest} from "../../services/assignor/dto/AssignorRequest.ts";
import {ApiError} from "../../api/errors/ApiError.ts";

export function useAssignors() {
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [creating, setCreating] = useState(false);
    const [searched, setSearched] = useState(false);

    const [assignor, setAssignor] = useState<AssignorResponse | null>(null);

    const findByDocumentNumber = useCallback((documentNumber: string) => {
        setLoading(true);
        setSearched(false);

        assignorService.findByDocumentNumber(documentNumber)
            .then(found => setAssignor(found))
            .catch(e => {
                if (e instanceof ApiError && e.status === 404) {
                    setAssignor(null);
                    return;
                }

                toast.error({ detail: e.message });
            })
            .finally(() => {
                setLoading(false);
                setSearched(true);
            });
    }, [toast]);

    const create = useCallback((request: AssignorRequest) => {
        const errors = validateAssignorRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }

        setCreating(true);
        return assignorService.create(request)
            .then(created => {
                toast.success({ detail: 'Cedente cadastrado com sucesso!' });
                setAssignor(created);
                return created;
            })
            .catch(e => { toast.error({ detail: e.message }); return null; })
            .finally(() => setCreating(false));
    }, [toast]);

    const reset = useCallback(() => {
        setAssignor(null);
        setSearched(false);
    }, []);

    return {
        assignor,
        loading,
        creating,
        searched,
        findByDocumentNumber,
        create,
        reset,
    };
}
