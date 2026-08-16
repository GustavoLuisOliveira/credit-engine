import React from "react";
import {Skeleton} from "primereact/skeleton";

export const Loading: React.FC = () => {
    return (
        <div className="flex justify-content-center">
            <Skeleton width="100%" height="150px"></Skeleton>
        </div>
    );
};
