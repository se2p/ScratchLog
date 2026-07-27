import numpy as np
import numpy.typing as npt


def progress_variance_projection(
    program_encodings: npt.NDArray[np.float64],
    start_project_repr: npt.NDArray[np.float64],
    solution_repr: npt.NDArray[np.float64],
) -> npt.NDArray[np.float64]:
    """
    Projects the program encodings into two-dimensional space according to the
    Progress-Variance projection.

    See Paassen, B., McBroom, J., Jeffries, B., Koprinska, I., & Yacef, K. (2021).
    Mapping Python Programs to Vectors using Recursive Neural Encodings. Journal of
    Educational Data Mining, 13(3), 1–35. https://doi.org/10.5281/zenodo.5634224;
    Algorithm 1.

    :param program_encodings: The program embeddings with one embedding vector per row
        and each row representing the embedding for one program state/edit step.
    :param start_project_repr: The program embedding of the initial program.
    :param solution_repr: The program embedding of the target solution.
    :return: The ``program_encodings`` mapped to 2-dimensional space. For an input with
        ``n`` many rows, the output has shape ``n×2``.
    """
    delta = solution_repr - start_project_repr
    delta_norm = np.linalg.norm(delta)
    delta = delta / np.linalg.norm(delta)

    x_init = program_encodings - start_project_repr
    # x in orthogonal space to delta
    x = x_init - x_init * np.dot(delta, delta.transpose())
    # quasi covariance matrix
    c = np.matmul(x.transpose(), x)
    # find eigenvector v for the largest eigenvalue of c
    eig_vals, eig_vs = np.linalg.eig(c)
    v = eig_vs[np.argmax(eig_vals)]

    # map to 2D
    y = x_init / delta_norm
    dv = np.stack((delta, v), axis=-1)
    return np.real(np.dot(y, dv))
